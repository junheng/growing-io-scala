package io.github.junheng.sdk.growingio

import java.io.File
import java.net.URL

import akka.actor.{ActorRef, Props}
import io.github.junheng.sdk.growingio.GetInsightsAction.{DownloadFailed, Insights, InsightsDownloaded}
import org.json4s.jackson.JsonMethods

import scala.sys.process._

class GetInsightsAction(client: String, id: String, token: String, time: String, reportTo: ActorRef) extends HttpAccessActor(client, token) {

  private val downloadFolder = new File(s"insights_${id}_$time")

  override def preStart() = {
    downloadFolder match {
      case folder if folder.exists() && folder.list().length >= 2 =>
        reportTo ! InsightsDownloaded(folder.list(), 0, 0)
        context.stop(self)
      case folder if folder.exists() => folder.listFiles().foreach(_.delete())
      case folder => folder.mkdir()
    }
    get(s"/insights/$id/$time.json?expire=120") {
      case resp: String => self ! JsonMethods.parse(resp).extract[Insights]
    }
  }


  override def receive: Receive = {
    case Insights(links) =>
      try {
        val start = System.currentTimeMillis()
        var totalFailed = 0
        val downloaded = links.map { link =>
          val fileName = if (link.contains("action")) "action.gz" else "pv.gz"
          log.info(s"downloading $fileName... ")
          val path = s"""${downloadFolder.getCanonicalPath}/$fileName"""
          def tryDownload = new URL(link).#>(new File(path)).!(ProcessLogger(x => log.debug("DOWNLOAD =>" + x)))
          var failed = 0
          while (failed < 5 && tryDownload != 0) failed += 1
          if (failed < 5) {
            log.info(s"downloaded $fileName")
          } else {
            throw DownloadFailed(link)
          }
          totalFailed += failed
          path
        }
        val cost = System.currentTimeMillis() - start
        reportTo ! InsightsDownloaded(downloaded, cost, totalFailed)
      } catch {
        case e: DownloadFailed =>
          reportTo ! e
          context.stop(self)
      }
  }
}

object GetInsightsAction {

  def props(client: String, id: String, token: String, time: String, reportTo: ActorRef) =
    Props(new GetInsightsAction(client, id, token, time, reportTo))

  case class DownloadFailed(link: String) extends Exception

  case class Download(links: String, file: String)

  case class Insights(downlinks: Seq[String])

  case class InsightsDownloaded(files: Seq[String], cost: Long, failed: Int)

}
