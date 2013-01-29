package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.ticket.actor.SearchActor._
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpPost, HttpGet}
import java.net.URI
import com.cysnake.ticket.actor.SocketActor.{Response, Request}
import akka.util.Timeout
import akka.util.duration._
import java.util
import org.apache.http.{HttpStatus, NameValuePair}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import com.cysnake.ticket.po.TicketPO
import org.apache.http.util.EntityUtils

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/23/13
 * Time: 5:47 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class SearchActor extends Actor with ActorLogging {
  implicit val timeout = Timeout(10 seconds)
  val socketActor = context.actorFor("/user/mainActor/socketActor")
  var ticket: TicketPO = null

  override def postRestart(reason: Throwable) {
    super.postRestart(reason)
    if (reason.isInstanceOf[OrderPageException]) {
      ticket = reason.asInstanceOf[OrderPageException].ticket
    } else if (reason.isInstanceOf[SearchTrainMatchException])
      ticket = reason.asInstanceOf[SearchTrainMatchException].ticket

    self ! SearchAllTrain
  }

  override def receive: Receive = {

    case StartSearchTrain(ticketPO) => {
      this.ticket = ticketPO
      self ! SearchAllTrain
    }

    case Response(response, httpRequest, requestType) => {
      requestType match {
        case SearchAllTrain => {
          val context1 = EntityUtils.toString(response.getEntity)
//          log.debug("searchAllTrain result:" + context1)
          try {
            val values = getArray(context1, ticket.trainName)
            ticket.fromCode = values(4)
            ticket.toCode = values(5)
            ticket.fromName = values(7)
            ticket.toName = values(8)
            ticket.startTime = values(2)
            ticket.endTime = values(6)
            ticket.trainCode = values(3)
            val formParams = new util.ArrayList[NameValuePair]
            formParams add new BasicNameValuePair("station_train_code", ticket.trainName)
            formParams add new BasicNameValuePair("train_date", ticket.date)
            formParams add new BasicNameValuePair("seattype_num", "")
            formParams add new BasicNameValuePair("from_station_telecode", ticket.fromCode)
            formParams add new BasicNameValuePair("to_station_telecode", ticket.toCode)
            formParams add new BasicNameValuePair("include_student", "00")
            formParams add new BasicNameValuePair("from_station_telecode_name", ticket.fromName)
            formParams add new BasicNameValuePair("to_station_telecode_name", ticket.toName)
            formParams add new BasicNameValuePair("round_train_date", ticket.date)
            formParams add new BasicNameValuePair("round_start_time_str", ticket.time)
            formParams add new BasicNameValuePair("single_round_type", "1")
            formParams add new BasicNameValuePair("train_pass_type", "QB")
            formParams add new BasicNameValuePair("train_class_arr", "QB#D#Z#T#K#QT#")
            formParams add new BasicNameValuePair("start_time_str", ticket.time)
            formParams add new BasicNameValuePair("lishi", values(1))
            formParams add new BasicNameValuePair("train_start_time", ticket.startTime)
            formParams add new BasicNameValuePair("trainno4", ticket.trainCode)
            formParams add new BasicNameValuePair("arrive_time", ticket.endTime)
            formParams add new BasicNameValuePair("from_station_name", ticket.fromName)
            formParams add new BasicNameValuePair("to_station_name", ticket.toName)
            formParams add new BasicNameValuePair("from_station_no", values(9))
            formParams add new BasicNameValuePair("to_station_no", values(10))
            formParams add new BasicNameValuePair("ypInfoDetail", values(11))
            formParams add new BasicNameValuePair("mmStr", values(12))
            formParams add new BasicNameValuePair("locationCode", values(13))
            val entity = new UrlEncodedFormEntity(formParams, "UTF-8")
            self ! PreOrder(entity)
          } catch {
            case ex: UnOrderAble => self ! SearchAllTrain
            case ex: Exception => throw ex
          }
        }

        case PreOrder(entity) => {
          httpRequest.releaseConnection()
          self ! GetOrderPage
        }

        case GetOrderPage => {
          if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
            val contextResponse = EntityUtils.toString(response.getEntity).trim.replaceAll("[\n\r]", "")
            val tokenRegx = """.*name\=\"org\.apache\.struts\.taglib\.html\.TOKEN\"\s*value\=\"(\w*?)\"\s*\>.*""".r
            val token = contextResponse match {
              case tokenRegx(value) => value
              case _ => ""
            }
            val leftTicketRegx = """.*orderRequest\.train\_no\"\s*value\=\"([a-z0-9A-Z_]*)\".*""".r
            val leftTicket = contextResponse match {
              case leftTicketRegx(value) => value
              case _ => ""
            }
            log.info("token is: " + token + " left token is: " + leftTicket)
            if (token != "" && leftTicket != "") {
              ticket.token = token
              ticket.leftTiketToken = leftTicket
              context.parent ! SearchSuccess(ticket)
            } else {
              log.info("can't get order page, retry ------------------")
              throw new OrderPageException(ticket)
            }
          } else {
            log.info("can't get order page, retry ------------------")
            throw new OrderPageException(ticket)
          }
          httpRequest.releaseConnection()
        }
      }
    }

    case GetOrderPage => {
      log.info("-------------------------GetOrderPage-------------------------------------")
      val path = "/head/8.getOrderPage.har"
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      socketActor ! Request(httpGet, GetOrderPage)
    }

    case PreOrder(entity) => {
      log.info("--------------------------preOrder----------------------------------------")
      val path = """/head/7.preOrder.har"""
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
      httpPost.setEntity(entity)
      socketActor ! Request(httpPost, PreOrder(entity))
    }

    case SearchAllTrain => {
      log.info("--------------------------searchAllTrain-----------------------------------")
      log.info("search Train: from->%s  to->%s  date->%s" format(ticket.searchFromCode, ticket.searchToCode, ticket.date))
      Thread.sleep(1000)
      val har = new HarEntity("/head/6.searchAllTrain.har")
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      val httpUrl = """https://dynamic.12306.cn/otsweb/order/querySingleAction.do?""" +
        """method=queryLeftTicket&orderRequest.train_date=""" + ticket.date +
        """&orderRequest.from_station_telecode=""" + ticket.searchFromCode +
        """&orderRequest.to_station_telecode=""" + ticket.searchToCode +
        """&orderRequest.train_no=&trainPassType=QB&trainClass=QB%23D%23Z%23T%23K%23QT%23&""" +
        """includeStudent=00&seatTypeAndNum=&orderRequest.start_time_str=00%3A00--24%3A00"""
      httpGet.setURI(new URI(httpUrl))
      socketActor ! Request(httpGet, SearchAllTrain)
    }

  }

  //  @throws(classOf[SearchTrainMatchException], classOf[UnOrderAble])
  private def getArray(source: String, trainNum: String): Array[String] = {
    val regx = """<span()"""
    val result = source.split(regx)
    var trainLine = ""
    for (re <- result) {
      val regxIsThisLine = (""".*onmouseout=\'onStopOut\(\)\'\>""" + trainNum + """\<\/span\>.*""").r
      if (!regxIsThisLine.findFirstMatchIn(re).isEmpty) {
        trainLine = re
      }
    }
    log.debug("match line is :" + trainLine)
    if (trainLine != "") {
      val regx2 = """.*javascript:getSelected\(\'(.*)\'\).*""".r
      val temp = trainLine match {
        case regx2(key) => key
        case _ =>
          log.warning("this train is out of order!!, retry ----->")
          throw new UnOrderAble("this train is out of order!!, retry ----->")
      }
      temp.split("#")
    } else {
      throw new SearchTrainMatchException("not find match train!! seems error here!", ticket)
    }
  }

}

object SearchActor {


  case class StartSearchTrain(ticket: TicketPO)

  private case class PreOrder(entity: UrlEncodedFormEntity)

  private case class SearchAllTrain()

  private case class GetOrderPage()

  case class SearchTrainMatchException(msg: String, ticket: TicketPO) extends RuntimeException(msg)

  case class OrderPageException(ticket: TicketPO) extends RuntimeException

  private case class UnOrderAble(msg: String) extends RuntimeException(msg)

  case class SearchSuccess(ticket: TicketPO)

}