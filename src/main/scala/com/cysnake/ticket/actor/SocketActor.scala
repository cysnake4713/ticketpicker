package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.ticket.http.HttpsUtil
import org.apache.http.client.methods.{HttpGet, HttpPost, HttpRequestBase}
import org.apache.http.{NameValuePair, HttpResponse}
import org.apache.http.client.utils.URLEncodedUtils
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
    val exception = reason.asInstanceOf[SocketActor.SocketException]
    self ! Request(exception.httpRequest, exception.requestType)
  }

  protected def receive: SocketActor#Receive = {

    case Request(httpRequest: HttpRequestBase, requestType: ScalaObject) => {
      log.debug("get request from:" + sender)
      log.debug("-------------------request url: %s----------------------" format httpRequest.getURI)
      log.debug("request method:" + httpRequest.getMethod)
      //      log.debug("request param:" + httpRequest.getParams)
      val headers = httpRequest.getAllHeaders
      for (header <- headers) {
        log.debug("header name: " + header.getName + " value: " + header.getValue)
      }
      if (httpRequest.isInstanceOf[HttpPost]) {
        val entity = httpRequest.asInstanceOf[HttpPost].getEntity
        val requestValues: java.util.List[NameValuePair] = URLEncodedUtils.parse(entity)
        log.debug("request form: " + requestValues)
      }
      if (httpRequest.isInstanceOf[HttpGet]) {
        log.debug("get request query: %s" format httpRequest.getURI.getQuery)
      }
      log.debug("cookis is :" + httpClient.asInstanceOf[DefaultHttpClient].getCookieStore.getCookies)
      try {
        val response = httpClient.execute(httpRequest)
        log.info("reponse status: " + response.getStatusLine)
        if (response.getEntity != null) {
          log.debug("response content type: %s" format response.getEntity.getContentType)
          log.debug("response content length: %d" format response.getEntity.getContentLength)
          log.debug("response content encoding: %s" format response.getEntity.getContentEncoding)
        }
        sender ! Response(response, httpRequest, requestType)
      } catch {
        case e: Exception =>
          log.error(e, "socket error!>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
          throw new SocketActor.SocketException(httpRequest, requestType)
      }

    }
  }
}

object SocketActor {

  case class SocketException(httpRequest: HttpRequestBase, requestType: ScalaObject) extends RuntimeException()

  case class Request(httpRequest: HttpRequestBase, requestType: ScalaObject)

  case class Response(response: HttpResponse, httpRequest: HttpRequestBase, requestType: ScalaObject)

}

