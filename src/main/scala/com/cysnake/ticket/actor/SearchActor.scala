package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.ticket.actor.SearchActor._
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet
import org.json.{JSONArray, JSONObject}
import scala.collection.mutable
import xml.XML
import java.net.{URLEncoder, URI}
import com.cysnake.ticket.actor.SocketActor.{Response, Request}
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import javax.swing.text.html.parser.Entity
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

  override def receive: Receive = {
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
      val xml = XML.load(getClass.getResource("/ticket.xml"))
      queryMap("orderRequest.from_station_telecode") = (xml \ "ticket" \ "from").text
      queryMap("orderRequest.to_station_telecode") = (xml \ "ticket" \ "to").text
      queryMap("orderRequest.train_date") = (xml \ "ticket" \ "date").text
      queryMap("orderRequest.start_time_str") = (xml \ "ticket" \ "time").text

      //add queryString to url
      val queryValue = ("" /: queryMap) {
        (result, ele) => result + "&" + ele._1 + "=" + ele._2
      }
      queryValue.substring(1)
      httpGet.setURI(new URI(httpGet.getURI + URLEncoder.encode(queryValue.substring(1), "UTF-8")))
      log.debug("query value is " + httpGet.getURI)
      //      httpGet.setURI(httpGet.getURI.getPath +)
      (socketActor ? Request(httpGet)).mapTo[Response] onSuccess {
        case Response(response) => {
          println("status:" + response.getStatusLine)
          println("context:" + EntityUtils.toString(response.getEntity))
          context.system.shutdown()
          //TODO:
        }
      }

    }

  }

}


object SearchActor {

  case class SearchAllTrain()

}