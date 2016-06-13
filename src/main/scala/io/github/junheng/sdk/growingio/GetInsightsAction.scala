package io.github.junheng.sdk.growingio

import java.io.{File, _}
import java.net.URL

import akka.actor.{ActorRef, Props}
import io.github.junheng.sdk.growingio.GetInsightsAction.{DownloadFailed, Insights, InsightsDownloaded}
import org.json4s.jackson.JsonMethods

import scala.sys.process._

class GetInsightsAction(client: String, id: String, token: String, time: String, reportTo: ActorRef) extends HttpAccessActor(client, token) {

  private val downloadFolder = new File(s"insights_${id}_$time")

  override def preStart() = {
    downloadFolder match {
      case folder if folder.exists() && folder.list().length >= 2 && folder.list().contains("finish_report") =>
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
      val start = System.currentTimeMillis()
      var totalFailed = 0
      try {
        links.map { link =>
          val fileName = if (link.contains("action")) "action.gz" else "pv.gz"
          log.info(s"downloading $fileName... ")
          val path = s"""${downloadFolder.getCanonicalPath}/$fileName"""
          def tryDownload = new URL(link).#>(new File(path)).!
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
        writeResult("/finish_report", links, totalFailed, cost(start))
        reportTo ! InsightsDownloaded(downloadFolder.getAbsolutePath, cost(start), totalFailed)
      } catch {
        case e: DownloadFailed =>
          writeResult("/failed_report", links, totalFailed, cost(start))
          reportTo ! e
          context.stop(self)
      }
  }

  def cost(start: Long): Long = System.currentTimeMillis() - start

  def writeResult(reportPath: String, links: Seq[String], totalFailed: Int, cost: Long): Unit = {
    val pw = new PrintWriter(new File(downloadFolder + reportPath))
    pw.write(s"files[${links.size}], cost[$cost], totalFailed[$totalFailed]")
    pw.close()
  }
}

object GetInsightsAction {

  def props(client: String, id: String, token: String, time: String, reportTo: ActorRef) =
    Props(new GetInsightsAction(client, id, token, time, reportTo))

  case class DownloadFailed(link: String) extends Exception

  case class Download(links: String, file: String)

  case class Insights(downlinks: Seq[String])

  case class InsightsDownloaded(folder: String, cost: Long, failed: Int)

}
