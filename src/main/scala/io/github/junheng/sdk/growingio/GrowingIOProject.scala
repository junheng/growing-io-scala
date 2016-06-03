package io.github.junheng.sdk.growingio


import akka.actor.Props
import io.github.junheng.sdk.growingio.GrowingIOProject.{GetInsights, Insights}
import org.json4s.jackson.JsonMethods


class GrowingIOProject(client: String, id: String, token: String) extends HttpAccessActor(client, token) {

  override def preStart(): Unit = log.debug("started")

  override def receive: Receive = {
    case GetInsights(time) =>
      val receipt = sender()
      get(s"/insights/$id/$time.json") { resp =>
        val insights = JsonMethods.parse(resp).extract[Insights]
        receipt ! insights
      }
  }
}

object GrowingIOProject {

  def props(client: String, id: String, token: String) = Props(new GrowingIOProject(client, id, token))

  case class GetInsights(time: String)

  case class Insights(downlinks: Seq[String])

}


