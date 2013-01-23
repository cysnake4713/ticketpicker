package com.cysnake.ticket.actor

import akka.actor._
import akka.util.duration._
import akka.util.Timeout
import akka.actor.SupervisorStrategy.Restart
import akka.event.LoggingReceive
import com.cysnake.ticket.actor.SearchActor._

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

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 4, withinTimeRange = 60 seconds) {
    case _: LoginActor.LoginException => Restart
    case _: SocketActor.SocketException => Restart
  }

  //  context.setReceiveTimeout(15 seconds)

  implicit val timeout = Timeout(10 seconds)

  val loginActor = context.watch(context.actorOf(Props[LoginActor], name = "loginActor"))
  val socketActor = context.watch(context.actorOf(Props[SocketActor], name = "socketActor"))
  val codeActor = context.actorOf(Props[CodeActor], name = "codeActor")
  val searchActor = context.actorOf(Props[SearchActor], name = "searchActor")

  override def receive: Receive = LoggingReceive {
    case StartMain => {
      loginActor ! GetCookie
    }

    case LoginSuccess => {
      searchActor ! SearchAllTrain
    }

    case ReceiveTimeout => {
      log.debug("receive timeout. shutdown now.")
      context.system.shutdown()

    }

    case Terminated(actorRef) if actorRef == loginActor =>
      log.debug("loginActor terminated. shutdown now.")
      context.system.shutdown()

    case _ => log.error(self + "match error")


  }
}

object MainActor {

  case class StartMain()

}

