package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.ticket.http.HttpsUtil
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.HttpResponse

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/18/13
 * Time: 2:12 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class SocketActor extends Actor with ActorLogging {

  import com.cysnake.ticket.actor.SocketActor._

  val httpClient = HttpsUtil.getHttpClient

  protected def receive: SocketActor#Receive = {

    case Send(httpRequest: HttpRequestBase) => {
      log.debug("get request from:" + sender)
      val response = httpClient.execute(httpRequest)
      sender ! Response(response)
    }
  }
}

object SocketActor {

  case class Send(httpRequest: HttpRequestBase)

  case class Response(response: HttpResponse)

}

