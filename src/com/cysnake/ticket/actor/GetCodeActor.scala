package com.cysnake.ticket.actor

import akka.actor.{Props, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet
import com.cysnake.ticket.ui.CodeFrame
import com.cysnake.ticket.http.HttpsUtil
import org.apache.http.util.EntityUtils

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 1:52 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */


class GetCodeActor extends Actor {
  var httpClient = HttpsUtil.getHttpClient


  import context._

  def receive = {
    case GetCode => {
      val path = """d:\ticket\head\loginPassCode.do.har"""
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest(httpClient).asInstanceOf[HttpGet]
      val response = httpClient.execute(httpGet)
      println("status: " + response.getStatusLine)
      val entity = response.getEntity
      val codeFrame = new CodeFrame
      val  stream = entity.getContent
      codeFrame.setImage(entity.getContent, this)
      codeFrame.startup(Array.empty)
      EntityUtils.consume(entity)
      stream.close()
      httpGet.abort()
      //      httpGet.releaseConnection()
    }

    case ResultCode(codeText: String) => {
      println("your input is: " + codeText)
      val loginActor = context.actorOf(Props[LoginActor], "loginActor")
      loginActor ! LoginFirst(codeText)
    }


  }

}


case class GetCode()

case class ResultCode(code: String)
