package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.ticket.http.HttpsUtil
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.{NoHttpResponseException, HttpResponse}
import org.apache.http.impl.client.DefaultHttpClient
import akka.actor.Status.Failure
import java.net.SocketException

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

    case Request(httpRequest: HttpRequestBase) => {
      log.debug("get request from:" + sender)
      log.debug("request url:" + httpRequest.getURI)
      log.debug("request method:" + httpRequest.getMethod)
//      log.debug("request param:" + httpRequest.getParams)
      log.debug("cookis is :" + httpClient.asInstanceOf[DefaultHttpClient].getCookieStore.getCookies)
      try {
        val response = httpClient.execute(httpRequest)
        sender ! Response(response)
      }catch{
        case ex:SocketException =>  sender ! Failure
      }

    }
  }
}

object SocketActor {

  case class Request(httpRequest: HttpRequestBase)

  case class Response(response: HttpResponse)

}

