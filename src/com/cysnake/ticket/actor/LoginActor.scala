package com.cysnake.ticket.actor

import akka.actor.Actor
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.json.{JSONTokener, JSONObject}
import java.io.InputStreamReader
import org.apache.http.util.EntityUtils
import com.cysnake.ticket.http.{JavaHttpsUtil, HttpsUtil}
import org.apache.http.client.HttpClient
import com.cysnake.ticket.ui.CodeFrame

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 5:09 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class LoginActor extends Actor {
  def receive = {
    case LoginFirst(code) => {
      println("login now")
      val httpClient = HttpsUtil.getHttpClient

      val path = """d:\ticket\head\loginPassCode.do.har"""
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest(httpClient).asInstanceOf[HttpGet]
      val response = httpClient.execute(httpGet)
      println("status: " + response.getStatusLine)
//      val entity = response.getEntity
//      val codeFrame = new CodeFrame
//      codeFrame.setImage(entity.getContent, this)
//      codeFrame.startup(Array.empty)
//      EntityUtils.consume(entity)
//      val path = """d:\ticket\head\loginAction.do.har"""
//      val har = new HarEntity(path)
//      val httpPost = har.generateHttpRequest(httpClient).asInstanceOf[HttpPost]
//
//      val response = httpClient.execute(httpPost)
//
//      println("status: " + response.getStatusLine)
//      val entity = response.getEntity
//      val CharsetPattern = """.*charset=(.*)""".r
//      val charset = entity.getContentType.getValue match {
//        case CharsetPattern(char) => char
//        case _ =>
//      }
//      val json = new JSONObject(new JSONTokener(new InputStreamReader(entity.getContent, charset.asInstanceOf[String])))
//      EntityUtils.consume(entity)
//      //      httpPost.releaseConnection()
//      val rand = json.get("loginRand").asInstanceOf[String]
//      println("rand number is: " + rand)
    }
  }
}


case class LoginFirst(code: String)