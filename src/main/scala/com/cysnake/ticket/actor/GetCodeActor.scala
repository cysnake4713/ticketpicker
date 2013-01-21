package com.cysnake.ticket.actor

import akka.actor.{ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet
import com.cysnake.ticket.ui.CodeFrame
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpStatus, HttpResponse}
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import com.cysnake.ticket.actor.GetCodeActor.GetCode
import scala.Predef._
import com.cysnake.ticket.actor.GetCodeActor.GetCode
import scala.AnyRef
import scala.Predef.AnyRef

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 1:52 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */


class GetCodeActor extends Actor with ActorLogging {


  implicit val timeout = Timeout(10 seconds)

  import com.cysnake.ticket.actor.SocketActor._

  def receive = {
    case GetCode(path: String) => {

      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      val socket = context.actorFor("../socketActor")

      log.debug(self + "send request to socketActor")
      (socket ? Send(httpGet)).mapTo[Response] onSuccess {
        case Response(response) => {
          log.debug("response status: " + response.getStatusLine)
          if (response.getStatusLine == HttpStatus.SC_OK) {
            val entity = response.getEntity
            val codeFrame = new CodeFrame(context.parent, "login")
            val stream = entity.getContent
            codeFrame.setImage(entity.getContent)
            codeFrame.startup(Array.empty)
            EntityUtils.consume(entity)
            stream.close()
            httpGet.releaseConnection()
          } else {
            //TODO
          }
        }
        case _ => log.debug("no match!!")
      }

    }
  }

}

object GetCodeActor {

  case class GetCode(path: String)

}

