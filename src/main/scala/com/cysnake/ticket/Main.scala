package com.cysnake.ticket

import actor.MainActor
import actor.MainActor._
import actor.ui.CodeDialog
import akka.actor.{Props, ActorSystem}
import util.CommonTools

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/16/13
 * Time: 6:24 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */


object Main {
  def main(args: Array[String]) {
    val account = try {
      CommonTools.createAccountFromFile(args(0))
    } catch {
      case ex: Exception => {
        println("account.xml load error!:" + ex.toString)
        sys.exit(1)
      }
    }
    val ticket = try {
      CommonTools.createTicketFromFile(args(1))
    } catch {
      case ex: Exception => {
        println("ticket.xml load error!:" + ex.toString)
        sys.exit(1)
      }
    }

    println("start")
    val system = ActorSystem("MySystem")
    val mainActor = system.actorOf(Props[MainActor], name = "mainActor")
    mainActor ! StartMain(account, ticket)
    Iterator.continually(Console.readLine()).takeWhile(_ != "exit").foreach(line => line match {
      case "exit" =>
        mainActor ! StopMain
        sys.exit(0)
        CodeDialog.dispose()
      case _ =>
    })

  }
}
