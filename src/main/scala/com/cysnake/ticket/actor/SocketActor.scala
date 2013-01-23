package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.ticket.http.HttpsUtil
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.HttpResponse
import org.apache.http.impl.client.DefaultHttpClient

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


  override def postRestart(reason: Throwable) {
    self ! Request(reason.asInstanceOf[SocketActor.SocketException].httpRequest)
  }

  protected def receive: SocketActor#Receive = {

    case Request(httpRequest: HttpRequestBase) => {
      log.debug("get request from:" + sender)
      log.debug("request url:" + httpRequest.getURI)
      log.debug("request method:" + httpRequest.getMethod)
      //      log.debug("request param:" + httpRequest.getParams)
      try {
        log.debug("cookis is :" + httpClient.asInstanceOf[DefaultHttpClient].getCookieStore.getCookies)
      } catch {
        case e: Exception => throw new SocketActor.SocketException(httpRequest)
      }
      val response = httpClient.execute(httpRequest)
      sender ! Response(response)

    }
  }
}

object SocketActor {

  case class SocketException(httpRequest: HttpRequestBase) extends RuntimeException()

  case class Request(httpRequest: HttpRequestBase)

  case class Response(response: HttpResponse)

}

