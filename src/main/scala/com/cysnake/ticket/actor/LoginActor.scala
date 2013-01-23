package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.json.{JSONTokener, JSONObject}
import java.io._
import org.apache.http.util.EntityUtils

import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import org.apache.http.{NameValuePair, HttpStatus}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import com.cysnake.ticket.actor.CodeActor.ReturnCodeResult
import com.cysnake.ticket.actor.CodeActor.GetCode
import java.util

//import com.cysnake.ticket.ui.CodeFrame

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
              case CharsetPattern(w) => w
            }
            val json = new JSONObject(new JSONTokener(new InputStreamReader(entity.getContent, charset)))
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
      val formParams = new util.ArrayList[NameValuePair]
      formParams add new BasicNameValuePair("loginRand", rand)
      formParams add new BasicNameValuePair("refundLogin", "N")
      formParams add new BasicNameValuePair("refundFlag", "Y")
      formParams add new BasicNameValuePair("loginUser.user_name", "dongcidaci1")
      formParams add new BasicNameValuePair("nameErrorFocus", "")
      formParams add new BasicNameValuePair("user.password", "36937004cys")
      formParams add new BasicNameValuePair("passwordErrorFocus", "")
      formParams add new BasicNameValuePair("randErrorFocus", "")
      formParams add new BasicNameValuePair("randCode", code)
      val entity = new UrlEncodedFormEntity(formParams, "UTF-8")
      httpPost.setEntity(entity)
      (socketActor ? Request(httpPost)).mapTo[Response] onSuccess {
        case Response(response) => {
          httpPost.releaseConnection()
          self ! IsLogin
        }
      }
    }


    case IsLogin => {
      val path = "/head/ticketPassCode.do.har"
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      (socketActor ? Request(httpGet)).mapTo[Response] onSuccess {
        case Response(response) => {
          log.debug("response status:" + response.getStatusLine)
          log.debug("content long: " + response.getEntity.getContentLength)
          if (response.getEntity.getContentLength != 0) {
            log.debug("IsLogin result:success")
            context.parent ! LoginSuccess
            httpGet.releaseConnection()
          } else {
            //TODO
            context.system.shutdown()
          }
        }
      }
    }
  }
}


object LoginActor {

  case class LoginFirst(code: String)

  case class LoginSecond(code: String, rand: String)

  case class GetCookie()

  case class IsLogin()

  case class LoginSuccess()

}

