package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Props, ReceiveTimeout, Actor}
import akka.util.duration._
import akka.pattern.ask
import akka.util.Timeout

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/18/13
 * Time: 11:17 AM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class MainActor extends Actor with ActorLogging {

  import com.cysnake.ticket.actor.MainActor._
  import com.cysnake.ticket.actor.LoginActor._

  context.setReceiveTimeout(12 seconds)
  implicit val timeout = Timeout(10 seconds)

  val loginActor = context.actorOf(Props[LoginActor], name = "loginActor")
  val socketActor = context.actorOf(Props[SocketActor], name = "socketActor")
  val codeActor = context.actorOf(Props[CodeActor], name = "codeActor")


  override def receive: Receive = {
    case StartMain => {
      loginActor ! GetCookie
    }

    case ReceiveTimeout => {
      log.debug("receive timeout. shutdown now.")
      context.system.shutdown()

    }

    case _ => log.error(self + "match error")


  }
}

object MainActor {

  case class StartMain()

}

