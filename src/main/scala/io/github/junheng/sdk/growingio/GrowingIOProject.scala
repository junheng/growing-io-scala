package io.github.junheng.sdk.growingio


import akka.actor.Props
import io.github.junheng.sdk.growingio.GrowingIOProject.DownloadInsights

import scala.language.postfixOps

class GrowingIOProject(client: String, id: String, token: String) extends HttpAccessActor(client, token) {

  override def preStart(): Unit = log.debug("started")

  override def receive: Receive = {
    case DownloadInsights(time) =>
      val actionPath = s"get_insight_action_$time"
      context.child(actionPath) match {
        case Some(exists) => log.info(s"$actionPath is running")
        case None => context.actorOf(GetInsightsAction.props(client, id, token, time, sender()),actionPath)
      }
  }
}

object GrowingIOProject {

  def props(client: String, id: String, token: String) = Props(new GrowingIOProject(client, id, token))

  case class DownloadInsights(time: String)

}


