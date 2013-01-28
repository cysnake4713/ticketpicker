package com.cysnake.ticket.actor

import akka.actor._
import akka.util.duration._
import akka.util.Timeout
import akka.actor.SupervisorStrategy.{Stop, Restart}
import akka.event.LoggingReceive
import com.cysnake.ticket.actor.SearchActor._
import com.cysnake.ticket.actor.CommitActor.{CommitSuccess, CommitFailure, StartCommit, FirstCommit}
import com.cysnake.ticket.po.{AccountPO, TicketPO}

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

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 4, withinTimeRange = 20.seconds) {
    case _: LoginActor.LoginException => Restart
    case _: SocketActor.SocketException => Restart
    case _: SearchTrainMatchException => Restart
    case _: OrderPageException => Restart
    case _: Exception => Stop
  }

  //  context.setReceiveTimeout(15 seconds)

  implicit val timeout = Timeout(10 seconds)

  val codeActor = context.actorOf(Props[CodeActor], name = "codeActor")
  val loginActor = context.watch(context.actorOf(Props[LoginActor], name = "loginActor"))
  val socketActor = context.watch(context.actorOf(Props[SocketActor], name = "socketActor"))
  val searchActor = context.watch(context.actorOf(Props[SearchActor], name = "searchActor"))
  val commitActor = context.watch(context.actorOf(Props[CommitActor], name = "commitActor"))

  var account: AccountPO = null
  var ticket: TicketPO = null

  override def receive: Receive = LoggingReceive {
    case StartMain(accountPO, ticketPO) => {
      this.account = accountPO
      this.ticket = ticketPO
      loginActor ! StartLogin(account)
    }

    case LoginSuccess => {
      log.debug("start search Train info.")
      searchActor ! StartSearchTrain(ticket)
    }

    case SearchSuccess(ticketPO) => {
      log.info("searchSuccess")
      //      ticket = ticketPO
      commitActor ! StartCommit(ticketPO)
    }

    case CommitSuccess => {
      context.system.shutdown()
      sys.exit(0)
    }

    case CommitFailure => {
      searchActor ! StartSearchTrain(ticket)
    }
    case Terminated(actorRef) if actorRef == loginActor =>
      log.debug("loginActor terminated. shutdown now.")
      context.system.shutdown()
      sys.exit(0)

    case Terminated(actorRef) if actorRef == searchActor =>
      log.debug("searchActor terminated. shutdown now.")
      context.system.shutdown()
      sys.exit(0)

    case Terminated(actorRef) => {
      log.debug("shit happens")
      context.system.shutdown()
      sys.exit(0)
    }

    case StopMain =>
      context.system.shutdown()
      sys.exit(0)

    case _ => log.error(self + "match error")


  }
}

object MainActor {

  case class StartMain(account: AccountPO, ticket: TicketPO)

  case class StopMain()

}

