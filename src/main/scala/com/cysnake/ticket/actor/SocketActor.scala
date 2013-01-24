package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.ticket.http.HttpsUtil
import org.apache.http.client.methods.{HttpPost, HttpRequestBase}
import org.apache.http.HttpResponse
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.utils.URLEncodedUtils

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
    val exception = reason.asInstanceOf[SocketActor.SocketException]
    self ! Request(exception.httpRequest, exception.requestType)
  }

  protected def receive: SocketActor#Receive = {

    case Request(httpRequest: HttpRequestBase, requestType: ScalaObject) => {
      log.debug("get request from:" + sender)
      log.debug("request url:" + httpRequest.getURI)
      log.debug("request method:" + httpRequest.getMethod)
      //      log.debug("request param:" + httpRequest.getParams)
      val headers = httpRequest.getAllHeaders
      for (header <- headers) {
        log.debug("header name: " + header.getName + " value: " + header.getValue)
      }
      if (httpRequest.isInstanceOf[HttpPost]) {
        val entity = httpRequest.asInstanceOf[HttpPost].getEntity
        val requestValues = URLEncodedUtils.parse(entity)
        log.debug("request form: " + requestValues)
      }
      log.debug("cookis is :" + httpClient.asInstanceOf[DefaultHttpClient].getCookieStore.getCookies)
      try {
        val response = httpClient.execute(httpRequest)
        log.info("reponse status: " + response.getStatusLine)
        sender ! Response(response, httpRequest, requestType)
      } catch {
        case e: Exception => throw new SocketActor.SocketException(httpRequest, requestType)
      }

    }
  }
}

object SocketActor {

  case class SocketException(httpRequest: HttpRequestBase, requestType: ScalaObject) extends RuntimeException()

  case class Request(httpRequest: HttpRequestBase, requestType: ScalaObject)

  case class Response(response: HttpResponse, httpRequest: HttpRequestBase, requestType: ScalaObject)

}

