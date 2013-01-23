package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.ticket.actor.SearchActor.Start
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/23/13
 * Time: 5:47 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class SearchActor extends Actor with ActorLogging {


  override def receive: Receive = {
    case Start => {
      val har = new HarEntity("/head/search-all-train.do.har")
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      log.debug("get url:" + httpGet.getURI.toASCIIString)
    }
  }
}


object SearchActor {

  case class Start()

}