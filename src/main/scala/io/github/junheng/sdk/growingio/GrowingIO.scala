package io.github.junheng.sdk.growingio

import akka.actor.{Actor, ActorLogging}

class GrowingIO extends Actor with ActorLogging {

  override def receive: Receive = authorizing

  def authorizing: Receive = {
    case _ => log.debug("WIP")
  }
}
