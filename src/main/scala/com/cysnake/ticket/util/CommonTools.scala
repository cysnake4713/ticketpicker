package com.cysnake.ticket.util

import com.cysnake.ticket.po.{AccountPO, TicketPO}
import xml.XML

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/28/13
 * Time: 4:14 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object CommonTools {
  def createTicketFromFile(path: String): TicketPO = {
    val ticketXml = XML.loadFile(path)
    val ticket = new TicketPO
    ticket.trainName = (ticketXml \ "ticket" \ "train").text
    ticket.searchFromName = (ticketXml \ "ticket" \ "from").text
    ticket.searchToName = (ticketXml \ "ticket" \ "to").text
    ticket.date = (ticketXml \ "ticket" \ "date").text
    ticket.time = (ticketXml \ "ticket" \ "time").text
    ticket.seat = (ticketXml \ "ticket" \ "seat").text
    ticket.passengerName = (ticketXml \ "ticket" \ "passenger" \ "name").text
    ticket.passengerId = (ticketXml \ "ticket" \ "passenger" \ "id").text
    ticket.passengerPhone = (ticketXml \ "ticket" \ "passenger" \ "phone").text
    ticket
  }

  def createAccountFromFile(path: String): AccountPO = {
    val AccountXml = XML.loadFile(path)
    val account = new AccountPO((AccountXml \ "account" \ "name").text, (AccountXml \ "account" \ "password").text)
    account
  }

  def createTicketStationMap: scala.collection.mutable.HashMap[String, String] = {
    val map = scala.collection.mutable.HashMap.empty[String, String]
    val lines = scala.io.Source.fromURL(getClass.getResource("/conf/station.file")).getLines()
    for (line <- lines) {
      val lineTemp = line.split('|')
      map += lineTemp(1) -> lineTemp(2)
    }
    map
  }
}
