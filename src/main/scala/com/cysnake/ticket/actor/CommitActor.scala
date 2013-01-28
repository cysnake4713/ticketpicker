package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpPost, HttpGet}
import java.net.URI
import com.cysnake.ticket.actor.SocketActor.{Response, Request}
import java.util
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.util.EntityUtils
import com.cysnake.ticket.po.TicketPO
import com.cysnake.ticket.actor.CommitActor._
import com.cysnake.ticket.actor.CodeActor.{GetCodeSuccess, GetCode}
import org.json.{JSONException, JSONObject}
import com.cysnake.ticket.actor.CommitActor.SecondCommit
import com.cysnake.ticket.actor.CodeActor.GetCode
import com.cysnake.ticket.actor.CommitActor.FinalCommit
import com.cysnake.ticket.actor.CommitActor.StartCommit
import com.cysnake.ticket.actor.SocketActor.Response
import com.cysnake.ticket.actor.SocketActor.Request
import com.cysnake.ticket.actor.CommitActor.FirstCommit
import com.cysnake.ticket.actor.CodeActor.GetCodeSuccess

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
  var code: String = ""


  var ticket: TicketPO = null

  override def receive: Receive = {

    case Response(response, httpRequest, requestType) => {
      requestType match {

        case FindUserInfo => {
          log.debug("result: " + EntityUtils.toString(response.getEntity))
          httpRequest.releaseConnection()
          self ! FirstCommit
        }
        case FinalCommit => {
          val target = EntityUtils.toString(response.getEntity)
          log.debug("entity: " + target)
          httpRequest.releaseConnection()

        }

        case FirstCommit => {
          val target = EntityUtils.toString(response.getEntity)
          log.debug("entity: " + target)
          try {
            val resultJson = new JSONObject(target)
            if (resultJson.get("errMsg").toString == "Y") {
              log.debug("first commit success!")
              self ! SecondCommit
            } else {
              context.parent ! CommitFailure
            }
            httpRequest.releaseConnection()

          } catch {
            case ex: JSONException => {
              log.info("first commit failure, reload")
              context.parent ! CommitFailure

            }
          }

        }

        case SecondCommit => {
          log.debug("entity is: " + EntityUtils.toString(response.getEntity))
          //TODO:
          self ! FinalCommit
        }
      }
    }
    ///////---------------------------------------------------------------------------------////////////////////////

    case SecondCommit => {
      log.debug("---------------------------SecondCommit--------------------------------------")
      val path = """/head/11.SecondCommit.har"""
      val httpGet = new HarEntity(path).generateHttpRequest.asInstanceOf[HttpGet]
      val httpUrl =
        ("""https://dynamic.12306.cn/otsweb/order/confirmPassengerAction.do?""" +
          """method=getQueueCount&train_date=%s&train_no=%s&""" +
          """station=%s&seat=%s&from=%s&to=%s&ticket=%s""")
          .format(ticket.date, ticket.trainCode, ticket.trainName,
          ticket.seat, ticket.fromCode, ticket.toCode, ticket.leftTiketToken)
      httpGet.setURI(new URI((httpUrl)))
      socketActor ! Request(httpGet, SecondCommit)
    }


    case GetCodeSuccess(codePO) => {
      log.debug("-------------------GetCodeSuccess: %s -----------------------------" format codePO)
      this.code = codePO
      self ! FirstCommit

    }

    case GetCode => {
      codeActor ! GetCode("/head/9.getCommitCode.har", self)
    }

    case FirstCommit => {
      log.debug("-------------------------firstCommit------------------------------")
      val path = "/head/10.firstCommit.har"
      val har = new HarEntity(path)
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
      httpPost.setURI(new URI(httpPost.getURI + "&rand=%s".format(code)))
      httpPost.setEntity(entity)
      Thread.sleep(5000)
      socketActor ! Request(httpPost, FirstCommit)
    }

    case FinalCommit => {
      Thread.sleep(5000)
      log.debug("---------------------------FinalCommit----------------------------------")
      val path = "/head/12.thirdCommit.har"
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
      formParams add new BasicNameValuePair("orderRequest.reserve_flag", "A")
      val entity = new UrlEncodedFormEntity(formParams, "UTF-8")
      httpPost.setEntity(entity)
      socketActor ! Request(httpPost, FinalCommit)

    }

    case FindUserInfo => {
      val path = """/head/13.temp.findAllPassenger.har"""
      val httpPost = new HarEntity(path).generateHttpRequest.asInstanceOf[HttpPost]
      socketActor ! Request(httpPost, FindUserInfo)
    }

    case StartCommit(ticketPO) => {
      ticket = ticketPO
      self ! GetCode
    }
  }
}


object CommitActor {

  case class StartCommit(ticket: TicketPO)

  private case class FirstCommit()

  private case class FinalCommit(code: String)

  private case class SecondCommit()

  case class CommitFailure()

  case class FindUserInfo()

}