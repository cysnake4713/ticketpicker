package com.cysnake.ticket.po

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/25/13
 * Time: 3:30 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class TicketPO {
  var fromCode: String = ""
  var fromName: String = ""
  var toName: String = ""
  var toCode: String = ""
  var trainName: String = ""
  var trainCode: String = ""
  var date: String = ""
  var time: String = ""
  var token: String = ""
  var leftTiketToken: String = ""
  var startTime: String = ""
  var endTime: String = ""
  val passengers = scala.collection.mutable.MutableList.empty[PassengerPO]
  var searchFromCode = ""
  var searchToCode = ""
  var searchFromName = ""
  var searchToName = ""
  var delay = 0

  override def toString: String = "from:%s to:%s passengers:%s" format(searchFromName, searchToName, passengers)
}
