package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpPost
import org.json.{JSONTokener, JSONObject}
import java.io.InputStreamReader
import org.apache.http.util.EntityUtils
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
class LoginActor extends Actor with ActorLogging {

  import com.cysnake.ticket.actor.SocketActor._
  import com.cysnake.ticket.actor.LoginActor._

  implicit val timeout = Timeout(10 seconds)


  def receive = {
    case LoginFirst(code) => {
      log.debug("login now")
      val path = """/head/loginAction.do.har"""
      val har = new HarEntity(path)
      val httpPost = har.generateHttpRequest.asInstanceOf[HttpPost]
      val socket = context.actorFor("../socketActor")
      (socket ? Send(httpPost)).onSuccess {
        case response: HttpResponse => {
          log.debug("status: " + response.getStatusLine)
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
      }
    }
  }
}

object LoginActor {

  case class LoginFirst(code: String)

}

