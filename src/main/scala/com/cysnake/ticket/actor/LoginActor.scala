package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.json.{JSONTokener, JSONObject}

import akka.util.Timeout
import akka.util.duration._
import org.apache.http.{NameValuePair, HttpStatus}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import com.cysnake.ticket.actor.CodeActor.{GetCodeFailure, GetCodeSuccess, GetCode}
import java.util
import javax.swing.ImageIcon
import javax.imageio.ImageIO
import com.cysnake.ticket.po.AccountPO
import org.apache.http.util.EntityUtils

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

  var account: AccountPO = null

  implicit val timeout = Timeout(10 seconds)


  override def postRestart(reason: Throwable) {
    log.debug("postRestart start working now! sender is :" + context.parent)
    this.account = reason.asInstanceOf[LoginException].account
    self ! GetCookie
  }

  def receive = {

    case StartLogin(accountPO) => {
      this.account = accountPO
      self ! GetCookie
    }


    case Response(response, httpRequest, requestFrom) => {
      if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
        requestFrom match {
          case GetCookie => {
            //            log.debug("get Cookie Response context is: %s" format EntityUtils.toString(response.getEntity))
            self ! GetLoginCode
          }
          case LoginFirst(code) => {
            val entityString = EntityUtils.toString(response.getEntity)
            log.debug("rand Entity string: %s" format entityString)
            val json = new JSONObject(new JSONTokener(entityString))
            val rand = json.get("loginRand").asInstanceOf[String]
            log.debug("rand number is: " + rand)
            httpRequest.releaseConnection()
            self ! LoginSecond(code, rand)
          }

          case LoginSecond(code, rand) => {
            httpRequest.releaseConnection()
            self ! IsLogin
          }
          case IsLogin => {
            try {
              new ImageIcon(ImageIO.read(response.getEntity.getContent))
            } catch {
              case e: Exception =>
                log.info("unable to login, retry ------------->")
                throw new LoginException(account)
            }
            finally {
              httpRequest.releaseConnection()
            }
            log.debug("IsLogin result: success")
            context.parent ! LoginSuccess

          }
        }
        httpRequest.releaseConnection()
      } else {
        httpRequest.releaseConnection()
        throw new LoginException(account)
      }
    }

    case GetCookie => {
      log.debug("===================GetCookie===========================")
      val path = "/head/1.getCookie.har"
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      socketActor ! Request(httpGet, GetCookie)
    }

    case GetLoginCode => {
      log.debug("send Get Code to getCodeActor")
      val path = "/head/2.getLoginCode.har"
      codeActor ! GetCode(path, self)
    }

    case GetCodeSuccess(code) => {
      log.info("get code Success, code = %s" format code)
      self ! LoginFirst(code)
    }

    case GetCodeFailure => {
      log.info("get code failure! retry -------------------------->>>>")
      throw new LoginException(account)
    }

    case LoginFirst(code) => {
      log.debug("------------------LoginFirst-----------------------")
      val path = """/head/3.FirstLogin.har"""
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
      socketActor ! Request(httpPost, LoginFirst(code))
    }

    case LoginSecond(code, rand) => {
      log.debug("--------------------------LoginSecond------------------------------")
      val path = "/head/4.Secondlogin.har"
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
      val formParams = new util.ArrayList[NameValuePair]
      formParams add new BasicNameValuePair("loginRand", rand)
      formParams add new BasicNameValuePair("refundLogin", "N")
      formParams add new BasicNameValuePair("refundFlag", "Y")
      formParams add new BasicNameValuePair("loginUser.user_name", account.name)
      formParams add new BasicNameValuePair("nameErrorFocus", "")
      formParams add new BasicNameValuePair("user.password", account.password)
      formParams add new BasicNameValuePair("passwordErrorFocus", "")
      formParams add new BasicNameValuePair("randErrorFocus", "")
      formParams add new BasicNameValuePair("randCode", code)
      val entity = new UrlEncodedFormEntity(formParams, "UTF-8")
      httpPost.setEntity(entity)
      socketActor ! Request(httpPost, LoginSecond(code, rand))
    }


    case IsLogin => {
      log.debug("-------------------------IsLogin-------------------------------")
      val path = "/head/5.isLogin.har"
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      socketActor ! Request(httpGet, IsLogin)
    }


  }
}


object LoginActor {

  case class StartLogin(account: AccountPO)

  case class LoginException(account: AccountPO) extends RuntimeException

  case class LoginSuccess()

  private case class LoginFirst(code: String)

  private case class LoginSecond(code: String, rand: String)

  private case class GetCookie()

  private case class IsLogin()


  private case class GetLoginCode()

}

