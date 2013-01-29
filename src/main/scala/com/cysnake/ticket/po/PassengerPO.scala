package com.cysnake.ticket.po

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/29/13
 * Time: 3:30 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class PassengerPO {
  var name = ""
  var id = ""
  var phone = ""
  var seat = ""

  override def toString: String = "name:%s id:%s seat:%s" format(name, id, seat)
}
