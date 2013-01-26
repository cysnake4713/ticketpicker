package com.cysnake.ticket

import actor.MainActor
import actor.MainActor._
import akka.actor.{Props, ActorSystem}
import po.{AccountPO, TicketPO}
import xml.XML

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/16/13
 * Time: 6:24 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */


object Main {
  def main(args: Array[String]) {
    val ticketXml = XML.load(getClass.getResource("/conf/ticket.xml"))
    val ticket = new TicketPO
    ticket.trainName = (ticketXml \ "ticket" \ "train").text
    ticket.fromCode = (ticketXml \ "ticket" \ "from").text
    ticket.toCode = (ticketXml \ "ticket" \ "to").text
    ticket.date = (ticketXml \ "ticket" \ "date").text
    ticket.time = (ticketXml \ "ticket" \ "time").text
    ticket.seat = (ticketXml \ "ticket" \ "seat").text
    ticket.passengerName = (ticketXml \ "ticket" \ "passenger" \ "name").text
    ticket.passengerId = (ticketXml \ "ticket" \ "passenger" \ "id").text
    ticket.passengerPhone = (ticketXml \ "ticket" \ "passenger" \ "phone").text

    val AccountXml = XML.load(getClass.getResource("/conf/account.xml"))
    val account = new AccountPO((AccountXml \ "account" \ "name").text, (AccountXml \ "account" \ "password").text)


    println("start")
    val system = ActorSystem("MySystem")
    val mainActor = system.actorOf(Props[MainActor], name = "mainActor")
    mainActor ! StartMain(account, ticket)
    Iterator.continually(Console.readLine()).takeWhile(_ != "exit").foreach(line => line match {
      case "stop" =>
        mainActor ! StopMain
        sys.exit(0)
      case _ =>
    })


    //    Login.getPage("""d:\ticket\head\loginpage.har""")

    //    Login.login1("""d:\ticket\head\loginAction.do.har""")
    //    Login.login("""d:\ticket\head\loginAction.do2.har""")
  }
}
