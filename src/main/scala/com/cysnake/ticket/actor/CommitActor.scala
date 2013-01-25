package com.cysnake.ticket.actor

import akka.actor.{Props, ActorLogging, Actor}
import com.cysnake.ticket.actor.SearchActor._
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpPost, HttpGet}
import org.json.{JSONArray, JSONObject}
import scala.collection.mutable
import xml.XML
import java.net.URI
import com.cysnake.ticket.actor.SocketActor.{Response, Request}
import akka.util.Timeout
import akka.util.duration._
import java.util
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.util.EntityUtils
import java.io.FileWriter
import com.cysnake.ticket.po.TicketPO
import com.cysnake.ticket.actor.CommitActor.{FirstCommit, FinalCommit}
import com.cysnake.ticket.actor.CodeActor.{ReturnCodeResult, GetCode}

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/25/13
 * Time: 4:41 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class CommitActor extends Actor with ActorLogging {
  val socketActor = context.actorFor("/user/mainActor/socketActor")
  val codeActor = context.actorFor("/user/mainActor/codeActor")
  var ticket: TicketPO = null

  override def receive: Receive = {

    case Response(response, httpRequest, requestType) => {
      requestType match {
        case FinalCommit(code) => {
          log.debug("status" + response.getStatusLine)
          val target = EntityUtils.toString(response.getEntity)
          println("entity: " + target)
          val file = new FileWriter("D:/ticket/test.html")
          file.write(target)
          file.close()
          httpRequest.releaseConnection()
        }

        case ReturnCodeResult(code) => {
          log.debug("status" + response.getStatusLine)
          val target = EntityUtils.toString(response.getEntity)
          println("entity: " + target)
          val file = new FileWriter("D:/ticket/test.html")
          file.write(target)
          file.close()
          httpRequest.releaseConnection()
        }
      }
    }


    case ReturnCodeResult(code) => {
      //first commit
      log.debug("ReturnCodeResult: " + code)
      val path = "/head/preConfirm.har"
      val har = new HarEntity(path)
      //TODO :
      ticket.passengerName = "asd"
      val ticketInfo = ticket.seat + ",0,1," + ticket.passengerName + ",1," + ticket.passengerId + "," + ticket.passengerPhone + ",N"
      val formParams = new util.ArrayList[NameValuePair]
      formParams add new BasicNameValuePair("org.apache.struts.taglib.html.TOKEN", ticket.token)
      formParams add new BasicNameValuePair("leftTicketStr", ticket.leftTiketToken)
      formParams add new BasicNameValuePair("textfield", "中文或拼音首字母")
      formParams add new BasicNameValuePair("orderRequest.train_date", ticket.date)
      formParams add new BasicNameValuePair("orderRequest.train_no", ticket.trainCode)
      formParams add new BasicNameValuePair("orderRequest.station_train_code", ticket.trainName)
      formParams add new BasicNameValuePair("orderRequest.from_station_telecode", ticket.fromCode)
      formParams add new BasicNameValuePair("orderRequest.to_station_telecode", ticket.toCode)
      formParams add new BasicNameValuePair("orderRequest.seat_type_code", "")
      formParams add new BasicNameValuePair("orderRequest.ticket_type_order_num", "")
      formParams add new BasicNameValuePair("orderRequest.bed_level_order_num", "000000000000000000000000000000")
      formParams add new BasicNameValuePair("orderRequest.start_time", ticket.startTime)
      formParams add new BasicNameValuePair("orderRequest.end_time", ticket.endTime)
      formParams add new BasicNameValuePair("orderRequest.from_station_name", ticket.fromName)
      formParams add new BasicNameValuePair("orderRequest.to_station_name", ticket.toName)
      formParams add new BasicNameValuePair("orderRequest.cancel_flag", "1")
      formParams add new BasicNameValuePair("orderRequest.id_mode", "Y")
      formParams add new BasicNameValuePair("passengerTickets", ticketInfo)
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("passenger_1_seat", ticket.seat)
      formParams add new BasicNameValuePair("passenger_1_ticket", "1")
      formParams add new BasicNameValuePair("passenger_1_name", ticket.passengerName)
      formParams add new BasicNameValuePair("passenger_1_cardtype", "1")
      formParams add new BasicNameValuePair("passenger_1_cardno", ticket.passengerId)
      formParams add new BasicNameValuePair("passenger_1_mobileno", ticket.passengerPhone)
      formParams add new BasicNameValuePair("checkbox9", "Y")
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("checkbox9", "Y")
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("checkbox9", "Y")
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("checkbox9", "Y")
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("checkbox9", "Y")
      formParams add new BasicNameValuePair("randCode", code)
      formParams add new BasicNameValuePair("tFlag", "dc")
      formParams add new BasicNameValuePair("orderRequest.reserve_flag", "A")
      val entity = new UrlEncodedFormEntity(formParams, "UTF-8")

      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
      httpPost.setURI(new URI(httpPost.getURI + code))
      httpPost.setEntity(entity)
      socketActor ! Request(httpPost, ReturnCodeResult(code))
    }

    case FirstCommit(ticketTemp) => {
      ticket = ticketTemp
      log.debug("firstCommit start")
      codeActor ! GetCode("/head/commitTicketCode.har", self)
    }

    case FinalCommit(code) => {
      val path = "/head/finalConfirm.har"
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]

      val formParams = new util.ArrayList[NameValuePair]
      //TODO: update the ticket info
      val ticketInfo = ticket.seat + ",0,1," + ticket.passengerName + ",1," + ticket.passengerId + "," + ticket.passengerPhone + ",N"

      formParams add new BasicNameValuePair("org.apache.struts.taglib.html.TOKEN", ticket.token)
      formParams add new BasicNameValuePair("leftTicketStr", ticket.leftTiketToken)
      formParams add new BasicNameValuePair("textfield", "中文或拼音首字母")
      formParams add new BasicNameValuePair("orderRequest.train_date", ticket.date)
      formParams add new BasicNameValuePair("orderRequest.train_no", ticket.trainCode)
      formParams add new BasicNameValuePair("orderRequest.station_train_code", ticket.trainName)
      formParams add new BasicNameValuePair("orderRequest.from_station_telecode", ticket.fromCode)
      formParams add new BasicNameValuePair("orderRequest.to_station_telecode", ticket.toCode)
      formParams add new BasicNameValuePair("orderRequest.seat_type_code", "")
      formParams add new BasicNameValuePair("orderRequest.ticket_type_order_num", "")
      formParams add new BasicNameValuePair("orderRequest.bed_level_order_num", "000000000000000000000000000000")
      formParams add new BasicNameValuePair("orderRequest.start_time", ticket.startTime)
      formParams add new BasicNameValuePair("orderRequest.end_time", ticket.endTime)
      formParams add new BasicNameValuePair("orderRequest.from_station_name", ticket.fromName)
      formParams add new BasicNameValuePair("orderRequest.to_station_name", ticket.toName)
      formParams add new BasicNameValuePair("orderRequest.cancel_flag", "1")
      formParams add new BasicNameValuePair("orderRequest.id_mode", "Y")
      formParams add new BasicNameValuePair("passengerTickets", ticketInfo)
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("passenger_1_seat", ticket.seat)
      formParams add new BasicNameValuePair("passenger_1_ticket", "1")
      formParams add new BasicNameValuePair("passenger_1_name", ticket.passengerName)
      formParams add new BasicNameValuePair("passenger_1_cardtype", "1")
      formParams add new BasicNameValuePair("passenger_1_cardno", ticket.passengerId)
      formParams add new BasicNameValuePair("passenger_1_mobileno", ticket.passengerPhone)
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("checkbox9", "Y")
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("checkbox9", "Y")
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("checkbox9", "Y")
      formParams add new BasicNameValuePair("oldPassengers", "")
      formParams add new BasicNameValuePair("checkbox9", "Y")
      formParams add new BasicNameValuePair("randCode", code)
      val entity = new UrlEncodedFormEntity(formParams, "UTF-8")
      httpPost.setEntity(entity)
      socketActor ! Request(httpPost, FinalCommit(code))

    }
  }
}


object CommitActor {

  case class FirstCommit(ticket: TicketPO)

  case class FinalCommit(code: String)

}