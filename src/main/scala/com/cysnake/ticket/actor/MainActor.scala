package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Props, ReceiveTimeout, Actor}
import akka.util.duration._
import akka.pattern.ask


/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/18/13
 * Time: 11:17 AM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class MainActor extends Actor with ActorLogging {

  import com.cysnake.ticket.actor.GetCodeActor._
  import com.cysnake.ticket.actor.MainActor._
  import com.cysnake.ticket.actor.LoginActor._

  context.setReceiveTimeout(30 seconds)

  val loginActor = context.actorOf(Props[LoginActor], name = "loginActor")
  val socketActor = context.actorOf(Props[SocketActor], name = "socketActor")
  val getCodeActor = context.actorOf(Props[GetCodeActor], name = "getCodeActor")

  override def receive: Receive = {
    case StartMain => {
      log.debug("send Get Code to getCodeActor")
      val path = """/head/passCodeAction.do.har"""
      getCodeActor ! GetCode(path)
    }

    case GetCodeResult(code, codeType) => {
      log.debug("get code Ruselt: " + code + " code type:" + codeType)
      codeType match {
        case "login" => {
          loginActor ! LoginFirst(code)

        }
      }
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

  case class GetCodeResult(code: String, codeType: String)

}

