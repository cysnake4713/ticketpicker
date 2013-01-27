package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.ticket.actor.SearchActor._
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpPost, HttpGet}
import org.json.{JSONArray, JSONObject}
import scala.collection.mutable
import xml.XML
import java.net.{URLEncoder, URI}
import com.cysnake.ticket.actor.SocketActor.{Response, Request}
import akka.util.Timeout
import akka.util.duration._
import java.util
import org.apache.http.NameValuePair
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
          //          response.getEntity.get
          //          val context1 = scala.io.Source.fromInputStream(response.getEntity.getContent, "UTF-8")
          //            .getLines().mkString("")
          val context1 = EntityUtils.toString(response.getEntity)
          try {
            //TODO
            val values = getArray(context1, ticket.trainName)
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
            formParams add new BasicNameValuePair("train_start_time", values(2))
            formParams add new BasicNameValuePair("trainno4", values(3))
            formParams add new BasicNameValuePair("arrive_time", values(6))
            formParams add new BasicNameValuePair("from_station_name", ticket.fromName)
            formParams add new BasicNameValuePair("to_station_name", ticket.toName)
            formParams add new BasicNameValuePair("from_station_no", values(9))
            formParams add new BasicNameValuePair("to_station_no", values(10))
            formParams add new BasicNameValuePair("ypInfoDetail", values(11))
            formParams add new BasicNameValuePair("mmStr", values(12))
            formParams add new BasicNameValuePair("locationCode", values(13))
            val entity = new UrlEncodedFormEntity(formParams, "UTF-8")
            self ! PreOrder(entity, ticket)
          } catch {
            case ex: UnOrderAble => self ! SearchAllTrain
            case ex: Exception => throw ex
          }
        }

        case PreOrder(entity, ticket) => {
          // TODO: when failure what do you do and how you define failure
          //         println(response.getStatusLine)
          //          val result = EntityUtils.toString(response.getEntity)
          //          val file = new FileWriter("D:/ticket/test.html")
          //          file.write(result)
          //          file.close()
          httpRequest.releaseConnection()
          self ! GetOrderPage(ticket)

        }

        case GetOrderPage(ticket) => {
          log.info("get order Page here ")
          val contextResponse = scala.io.Source.fromInputStream(response.getEntity.getContent, "UTF-8")
            .getLines().mkString("")

          val tokenRegx = """.*name\=\"org\.apache\.struts\.taglib\.html\.TOKEN\"\s*value\=\"(\w{32})\"\>.*""".r
          val token = contextResponse match {
            case tokenRegx(value) => value
            case _ => ""
          }
          val leftTicketRegx = """.*name=\"leftTicketStr\"\s*id=\"left\_ticket\"\s*value\=\"(\w{30})\".*""".r
          val leftTicket = contextResponse match {
            case leftTicketRegx(value) => value
            case _ => ""
          }
          log.debug("token is: " + token + " left token is: " + leftTicket)
          if (token != "" && leftTicket != "") {
            ticket.token = token
            ticket.leftTiketToken = leftTicket
            context.parent ! SearchSuccess(ticket)

          } else {
            log.info("can't get order page, retry ------------------")
            self ! SearchAllTrain
          }




          //          val file = new FileWriter("D:/ticket/test" + i + ".html")
          //          i += 1
          //          file.write(context1)
          //          file.close()
          httpRequest.releaseConnection()
          //          context.system.shutdown()
        }
      }
    }

    case GetOrderPage(ticket) => {
      val path = "/head/getCookie.harar"
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      socketActor ! Request(httpGet, GetOrderPage(ticket))
    }

    case PreOrder(entity, ticketPO) => {
      log.debug("preOrder")
      val path = """/head/search-commit.har"""
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
      httpPost.setEntity(entity)
      socketActor ! Request(httpPost, PreOrder(entity, ticketPO))
    }

    case SearchAllTrain => {
      val har = new HarEntity("/head/search-all-train.do.har")
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]

      //set queryString
      val queryString = har.getJson.get("request").asInstanceOf[JSONObject]
        .get("queryString").asInstanceOf[JSONArray]
      val queryMap = mutable.Map.empty[String, String]
      for (i <- 0 to queryString.length - 1) {
        val queryJson = queryString.getJSONObject(i)
        queryMap += queryJson.get("name").toString -> queryJson.get("value").toString
      }
      //set ticket information
      //TODO:
      //      queryMap("orderRequest.from_station_telecode") = (xml \ "ticket" \ "from").text
      //      queryMap("orderRequest.to_station_telecode") = (xml \ "ticket" \ "to").text
      //      queryMap("orderRequest.train_date") = (xml \ "ticket" \ "date").text
      //      queryMap("orderRequest.start_time_str") = URLEncoder.encode((xml \ "ticket" \ "time").text)

      //add queryString to url
      val queryValue = ("" /: queryMap) {
        (result, ele) => result + "&" + ele._1 + "=" + ele._2
      }
      queryValue.substring(1)
      //      httpGet.setURI(new URI(httpGet.getURI + "?" + URLEncoder.encode(queryValue.substring(1), "UTF-8")))
      httpGet.setURI(new URI(httpGet.getURI + "?" + queryValue.substring(1)))
      //      httpGet.setURI(new URI( """https://dynamic.12306.cn/otsweb/order/querySingleAction.do?method=queryLeftTicket&orderRequest.train_date=2013-02-02&orderRequest.from_station_telecode=SZQ&orderRequest.to_station_telecode=GGQ&orderRequest.train_no=&trainPassType=QB&trainClass=QB%23D%23Z%23T%23K%23QT%23&includeStudent=00&seatTypeAndNum=&orderRequest.start_time_str=00%3A00--24%3A00"""))
      socketActor ! Request(httpGet, SearchAllTrain)
    }

  }

  //  @throws(classOf[SearchTrainMatchException], classOf[UnOrderAble])
  private def getArray(source: String, trainNum: String): Array[String] = {
    val regx = """<span()"""
    log.debug("source is: " + source)
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
          log.info("this train is out of order!!, retry ----->")
          throw new UnOrderAble("this train is out of order!!, retry ----->")
      }
      temp.split("#")
    } else {
      throw new SearchTrainMatchException("not find match train!! seems error here!")
    }
  }

}

object SearchActor {


  case class StartSearchTrain(ticket: TicketPO)

  private case class PreOrder(entity: UrlEncodedFormEntity, ticket: TicketPO)

  private case class SearchAllTrain()

  private case class GetOrderPage(ticket: TicketPO)

  case class SearchTrainMatchException(msg: String) extends RuntimeException(msg)

  private case class UnOrderAble(msg: String) extends RuntimeException(msg)

  case class SearchSuccess(ticket: TicketPO)

}