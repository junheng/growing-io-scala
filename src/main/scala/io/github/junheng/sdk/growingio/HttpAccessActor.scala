package io.github.junheng.sdk.growingio

import akka.actor.{Actor, ActorLogging}
import dispatch._
import io.github.junheng.sdk.growingio.HttpAccessActor._
import org.json4s.DefaultFormats

abstract class HttpAccessActor(client: String, token: String) extends Actor with ActorLogging {

  import context.dispatcher

  protected implicit val format = DefaultFormats

  def get(path: String)(callback: PartialFunction[Any, Unit]) = {
    val req = url(ENDPOINT + path)
      .addHeader(H_CLIENT_ID, client)
      .addHeader(H_TOKEN, token)
      .GET

    process(req, callback)
  }

  def process(req: Req, callback: PartialFunction[Any, Unit]): Future[Unit] = {
    Http(
      req > { resp =>
        log.debug(s"${req.toRequest.toString} returned [${resp.getStatusText}]:\n${resp.getResponseBody}")
        resp.getStatusCode match {
          case 200 => callback(resp.getResponseBody)
          case error => callback(error, resp.getResponseBody)
        }
      }
    )
  }
}

object HttpAccessActor {
  final val ENDPOINT = "https://gta.growingio.com"
  final val H_CLIENT_ID = "X-Client-Id"
  final val H_TOKEN = "Authorization"
  final val P_PROJECT_ID = "ai"
  final val P_PROJECT_UID = "project"
  final val P_ENCRYPTED_TOKEN = "auth"
  final val P_TM = "tm"
}
