package com.cysnake.ticket.actor

import akka.actor.{Props, ReceiveTimeout, Actor}
import akka.util.duration._


/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/18/13
 * Time: 11:17 AM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class MainActor extends Actor {
  //  implicit val httpClient = HttpsUtil.getHttpClient

  import context._

  context.setReceiveTimeout(15 seconds)

  val loginActor = context.actorOf(Props[LoginActor], name = "loginActor")
  val socketActor = context.actorOf(Props[SocketActor], name = "socketActor")
  val getCodeActor = context.actorOf(Props[GetCodeActor], name = "getCodeActor")

  //  override def preStart() {
  //    super.preStart()
  //    println("start")
  //  }
  //
  //
  //  override def postStop() {
  //    super.postStop()
  //    println("stop")
  //  }

  override def receive: Receive = {
    case StartMain => {
      getCodeActor ! GetCode
    }

    case GetCodeRuselt(code) => {
      println("get code Ruselt")
      loginActor ! LoginFirst(code)
    }

    case ReceiveTimeout => context.system.shutdown()

    case _ => println("match error")


  }
}


case class StartMain()

case class GetCodeRuselt(code: String)