package com.cysnake.ticket.actor

import akka.actor.{OneForOneStrategy, SupervisorStrategy, ActorLogging, Actor}
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
import akka.actor.SupervisorStrategy.Restart
import java.lang.reflect.UndeclaredThrowableException

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

  val socketActor = context.actorFor("/user/mainActor/socketActor")
  val codeActor = context.actorFor("/user/mainActor/codeActor")

  implicit val timeout = Timeout(10 seconds)


  override def postRestart(reason: Throwable) {
    log.debug("postRestart start working now! sender is :" + context.parent)
    self ! GetLoginCode
  }

  def receive = {


    case GetCookie => {
      log.debug("GetCookie")
      val path = """/head/getCookie.har"""
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      (socketActor ? Request(httpGet)).mapTo[Response] onSuccess {
        case Response(response) => {
          log.debug("status:" + response.getStatusLine)
          httpGet.releaseConnection()
          self ! GetLoginCode
        }
      }

    }

    case GetLoginCode => {
      log.debug("send Get Code to getCodeActor")
      val path = """/head/passCodeAction.do.har"""
      codeActor ! GetCode(path, self)
    }

    case ReturnCodeResult(code) => {
      self ! LoginFirst(code)
    }

    case LoginFirst(code) => {
      log.debug("LoginFirst")
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
            self ! ThrowExecption
          }
        }
      }
    }

    case LoginSecond(code, rand) => {
      log.debug("LoginSecond")
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
      log.debug("IsLogin")
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
            log.debug("login result: failure!!")
            self ! ThrowExecption
          }
        }
      }
    }
    case ThrowExecption => throw new LoginException("unable to login!!")

  }
}


object LoginActor {

  case class ThrowExecption()

  case class LoginException(msg: String) extends RuntimeException(msg)

  case class LoginFirst(code: String)

  case class LoginSecond(code: String, rand: String)

  case class GetCookie()

  case class IsLogin()

  case class LoginSuccess()

  case class GetLoginCode()

}

