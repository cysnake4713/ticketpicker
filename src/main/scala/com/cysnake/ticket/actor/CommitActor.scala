package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpPost, HttpGet}
import java.net.URI
import java.util
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.util.EntityUtils
import com.cysnake.ticket.po.TicketPO
import com.cysnake.ticket.actor.CommitActor._
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
          httpRequest.releaseConnection()
          log.info("entity: " + target)
          val result = new JSONObject(target)
          if (result.get("errMsg").toString == "Y") {
            log.info("order success!!! quit!-*****************************************>>>>>>>>>>>>>>>>>")
            context.parent ! CommitSuccess
          } else {
            context.parent ! CommitFailure
          }
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
          self ! FinalCommit
        }
      }
    }
    ///////---------------------------------------------------------------------------------////////////////////////

    case SecondCommit => {
      log.info("---------------------------SecondCommit--------------------------------------")
      val path = """/head/11.SecondCommit.har"""
      val httpGet = new HarEntity(path).generateHttpRequest.asInstanceOf[HttpGet]
      val httpUrl =
        ("""https://dynamic.12306.cn/otsweb/order/confirmPassengerAction.do?""" +
          """method=getQueueCount&train_date=%s&train_no=%s&""" +
          """station=%s&seat=%s&from=%s&to=%s&ticket=%s""")
          .format(ticket.date, ticket.trainCode, ticket.trainName,
          ticket.passengers(0).seat, ticket.fromCode, ticket.toCode, ticket.leftTiketToken)
      httpGet.setURI(new URI((httpUrl)))
      socketActor ! Request(httpGet, SecondCommit)
    }


    case GetCodeSuccess(codePO) => {
      log.info("-------------------GetCodeSuccess: %s -----------------------------" format codePO)
      this.code = codePO
      self ! FirstCommit

    }

    case GetCode => {
      codeActor ! GetCode("/head/9.getCommitCode.har", self)
    }

    case FirstCommit => {
      log.info("-------------------------firstCommit------------------------------")
      val path = "/head/10.firstCommit.har"
      val har = new HarEntity(path)
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
      //----------------------
      var i = 1
      for (passenger <- ticket.passengers) {
        val ticketInfo = passenger.seat + ",0,1," + passenger.name + ",1," +
          passenger.id + "," + passenger.phone + ",N"
        formParams add new BasicNameValuePair("passengerTickets", ticketInfo)
        formParams add new BasicNameValuePair("oldPassengers", "")
        formParams add new BasicNameValuePair("passenger_%d_seat" format i, passenger.seat)
        formParams add new BasicNameValuePair("passenger_%d_ticket" format i, "1")
        formParams add new BasicNameValuePair("passenger_%d_name" format i, passenger.name)
        formParams add new BasicNameValuePair("passenger_%d_cardtype" format i, "1")
        formParams add new BasicNameValuePair("passenger_%d_cardno" format i, passenger.id)
        formParams add new BasicNameValuePair("passenger_%d_mobileno" format i, passenger.phone)
        i += 1
      }
      //-------------------------
      for (i <- 1 to (5 - ticket.passengers.length)) {
        formParams add new BasicNameValuePair("oldPassengers", "")
        formParams add new BasicNameValuePair("checkbox9", "Y")
      }
      formParams add new BasicNameValuePair("randCode", code)
      formParams add new BasicNameValuePair("tFlag", "dc")
      formParams add new BasicNameValuePair("orderRequest.reserve_flag", "A")
      val entity = new UrlEncodedFormEntity(formParams, "UTF-8")

      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
      httpPost.setURI(new URI(httpPost.getURI + "&rand=%s".format(code)))
      httpPost.setEntity(entity)

      socketActor ! Request(httpPost, FirstCommit)
    }

    case FinalCommit => {
      Thread.sleep(ticket.delay * 1000)
      log.info("---------------------------FinalCommit----------------------------------")
      val path = "/head/12.thirdCommit.har"
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
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

      var i = 1
      for (passenger <- ticket.passengers) {
        val ticketInfo = passenger.seat + ",0,1," + passenger.name + ",1," +
          passenger.id + "," + passenger.phone + ",N"
        formParams add new BasicNameValuePair("passengerTickets", ticketInfo)
        formParams add new BasicNameValuePair("oldPassengers", "")
        formParams add new BasicNameValuePair("passenger_%d_seat" format i, passenger.seat)
        formParams add new BasicNameValuePair("passenger_%d_ticket" format i, "1")
        formParams add new BasicNameValuePair("passenger_%d_name" format i, passenger.name)
        formParams add new BasicNameValuePair("passenger_%d_cardtype" format i, "1")
        formParams add new BasicNameValuePair("passenger_%d_cardno" format i, passenger.id)
        formParams add new BasicNameValuePair("passenger_%d_mobileno" format i, passenger.phone)
        i += 1
      }
      for (i <- 1 to (5 - ticket.passengers.length)) {
        formParams add new BasicNameValuePair("oldPassengers", "")
        formParams add new BasicNameValuePair("checkbox9", "Y")
      }
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
      log.info("------------------------startCommit--------------------------------------")
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


  private case class FindUserInfo()

  case class CommitFailure()

  case class CommitSuccess()

}