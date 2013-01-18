package com.cysnake.ticket.actor

import akka.actor.Actor
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.json.{JSONTokener, JSONObject}
import java.io.InputStreamReader
import org.apache.http.util.EntityUtils
import com.cysnake.ticket.http.HttpsUtil
import org.apache.http.client.HttpClient
import com.cysnake.ticket.ui.CodeFrame
import org.apache.http.HttpResponse

import akka.dispatch.Future
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 5:09 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class LoginActor extends Actor {
  implicit val timeout = Timeout(10 seconds)

  import context._

  def receive = {
    case LoginFirst(code) => {
      println("login now")
      //      val httpClient = HttpsUtil.getHttpClient

      //      val path = """d:\ticket\head\loginPassCode.do.har"""
      //      val har = new HarEntity(path)
      //      val httpGet = har.generateHttpRequest(httpClient).asInstanceOf[HttpGet]
      //      val response = httpClient.execute(httpGet)
      //      println("status: " + response.getStatusLine)
      //      val entity = response.getEntity
      //      val codeFrame = new CodeFrame
      //      codeFrame.setImage(entity.getContent, this)
      //      codeFrame.startup(Array.empty)
      //      EntityUtils.consume(entity)


      val path = """d:\ticket\head\loginAction.do.har"""
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
      val socket = context.actorFor("../socketActor")
      val future: Future[Response] = ask(socket, Send(httpPost)).mapTo[Response]
      future.map(ele => ele match {
        case Response(response: HttpResponse) => {
          println("status: " + response.getStatusLine)
          val entity = response.getEntity
          val CharsetPattern = """.*charset=(.*)""".r
          val charset = entity.getContentType.getValue match {
            case CharsetPattern(char) => char
            case _ =>
          }
          val json = new JSONObject(new JSONTokener(new InputStreamReader(entity.getContent, charset.asInstanceOf[String])))
          EntityUtils.consume(entity)
          //      httpPost.releaseConnection()
          val rand = json.get("loginRand").asInstanceOf[String]
          println("rand number is: " + rand)

        }
      })


    }
  }
}


case class LoginFirst(code: String)