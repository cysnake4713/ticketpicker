package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.json.{JSONTokener, JSONObject}
import java.io.InputStreamReader
import org.apache.http.util.EntityUtils

import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import com.cysnake.ticket.actor.CodeActor.{ReturnCodeResult, GetCode}
import org.apache.http.HttpStatus

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 5:09 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class LoginActor extends Actor with ActorLogging {

  import com.cysnake.ticket.actor.SocketActor._
  import com.cysnake.ticket.actor.LoginActor._

  val socketActor = context.actorFor("../socketActor")
  val codeActor = context.actorFor("../codeActor")

  implicit val timeout = Timeout(10 seconds)

  def receive = {


    case GetCookie => {
      val path = """/head/getCookie.har"""
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      (socketActor ? Request(httpGet)).mapTo[Response] onSuccess {
        case Response(response) => {
          log.debug("status:" + response.getStatusLine)
          log.debug("setcookie value:" + response.getHeaders("Set-Cookie")(0).getValue)
          httpGet.releaseConnection()
          log.debug("send Get Code to getCodeActor")
          val path = """/head/passCodeAction.do.har"""
          codeActor ! GetCode(path, self)
        }
      }

    }

    case ReturnCodeResult(code) => {
      self ! LoginFirst(code)
    }

    case LoginFirst(code) => {
      log.debug("login now")
      val path = """/head/loginAction.do.har"""
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
      (socketActor ? Request(httpPost)).mapTo[Response] onSuccess {
        case Response(response) => {
          log.debug("status: " + response.getStatusLine)
          if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
            val entity = response.getEntity
            val CharsetPattern = """.*charset=(.*)""".r
            val charset = entity.getContentType.getValue match {
              case CharsetPattern(code) => code
            }
            val json = new JSONObject(new JSONTokener(new InputStreamReader(entity.getContent, charset.asInstanceOf[String])))
            EntityUtils.consume(entity)
            //      httpPost.releaseConnection()
            val rand = json.get("loginRand").asInstanceOf[String]
            log.debug("rand number is: " + rand)
            httpPost.releaseConnection()
            self ! LoginSecond(code, rand)
          } else {
            //TODO
          }
        }
      }
    }

    case LoginSecond(code, rand) => {
      val path = """/head/loginAction2.do.har"""
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]

    }
  }
}

object LoginActor {

  case class LoginFirst(code: String)

  case class LoginSecond(code: String, rand: String)

  case class GetCookie()

}

