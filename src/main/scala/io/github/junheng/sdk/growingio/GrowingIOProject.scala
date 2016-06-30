package io.github.junheng.sdk.growingio


import java.text.SimpleDateFormat
import java.util.Calendar

import akka.actor.Props
import io.github.junheng.sdk.growingio.GrowingIOProject.{DownloadCurrentInsights, DownloadInsights}

import scala.language.postfixOps

class GrowingIOProject(client: String, id: String, token: String) extends HttpAccessActor(client, token) {

  override def preStart(): Unit = log.debug("started")

  override def receive: Receive = {
    case DownloadCurrentInsights => self forward DownloadInsights(pastHour)
    case DownloadInsights(time) =>
      val actionPath = s"get_insight_action_$time"
      context.child(actionPath) match {
        case Some(exists) => //still downloading, do nothing
        case None => context.actorOf(GetInsightsAction.props(client, id, token, time, sender()),actionPath)
      }
  }

  def pastHour: String = {
    val instance: Calendar = Calendar.getInstance()
    instance.add(Calendar.HOUR, -2)
    val time = new SimpleDateFormat("yyyyMMddHH").format(instance.getTime)
    time
  }
}

object GrowingIOProject {

  def props(client: String, id: String, token: String) = Props(new GrowingIOProject(client, id, token))

  case class DownloadInsights(time: String)

  case object DownloadCurrentInsights

}


