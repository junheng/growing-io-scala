package io.github.junheng.sdk.growingio

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import akka.actor.{Actor, ActorLogging, Props}
import dispatch._
import io.github.junheng.sdk.growingio.GrowingIOService.{Authorization, GetGrowingIOProject}
import io.github.junheng.sdk.growingio.HttpAccessActor._
import org.apache.commons.codec.binary.Hex
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

class GrowingIOService extends Actor with ActorLogging {

  import context.dispatcher

  private implicit val format = DefaultFormats

  override def receive: Receive = {
    case GetGrowingIOProject(client, secret, id, uid) =>
      val encryptedToken = encrypt(secret, id, uid)
      val receipt = sender()
      val path: String = "/auth/token"
      val req = url(ENDPOINT + path)
        .addHeader(H_CLIENT_ID, client)
        .addQueryParameter(P_PROJECT_ID, id)
        .addQueryParameter(P_PROJECT_UID, uid)
        .addQueryParameter(P_ENCRYPTED_TOKEN, encryptedToken)
        .POST

      Http(req > { resp =>
        log.debug(s"returned [${resp.getStatusText}] of 'POST $path':\n${resp.getResponseBody}")
        resp.getStatusCode match {
          case 200 =>
            val json = JsonMethods.parse(resp.getResponseBody)
            val authorization = json.extract[Authorization]
            receipt ! context.actorOf(GrowingIOProject.props(client, id, authorization.code), id)
          case error =>
            log.error(s"error [$error]:\n" + resp.getResponseBody)
        }
      })
  }

  def encrypt(secret: String, id: String, uid: String) = {
    val messages = s"POST\n/auth/token\nproject=$uid&ai=$id"
    val hmac = Mac.getInstance("HmacSHA256")
    hmac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"))
    val signature = hmac.doFinal(messages.getBytes("UTF-8"))
    Hex.encodeHexString(signature)
  }
}

object GrowingIOService {

  def props = Props[GrowingIOService]

  case class GetGrowingIOProject(client: String, secret: String, id: String, uid: String)

  case class Authorization(status: String, code: String)

}