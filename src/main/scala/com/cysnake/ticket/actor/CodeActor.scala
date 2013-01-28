package com.cysnake.ticket.actor

import akka.actor.{ActorRef, ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.HttpStatus
import scala.Predef._
import com.cysnake.ticket.actor.CodeActor.{GetCodeFailure, GetCode}
import javax.swing.ImageIcon
import javax.imageio.ImageIO
import ui.CodeDialog

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 1:52 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */


class CodeActor extends Actor with ActorLogging {

  import com.cysnake.ticket.actor.SocketActor._


  override def preStart() {
    super.preStart()
    CodeDialog.centerOnScreen()
  }


  override def postStop() {
    super.postStop()
    CodeDialog.dispose()
  }

  def receive = {
    case Response(response, httpRequest, requestType) => {
      requestType match {
        case GetCode(path, sourceActor) => {
          try {
            if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
              val entity = response.getEntity
              val imageIcon = new ImageIcon(ImageIO.read(entity.getContent))
              CodeDialog.start(imageIcon, sourceActor)
            } else {
              sourceActor ! GetCodeFailure
            }
          } catch {
            case ex: Exception =>
              log.info("parse image fail, retry -------------->")
              sourceActor ! GetCodeFailure
          }
        }
      }
      httpRequest.releaseConnection()
    }

    case GetCode(path: String, sourceActor) => {
      log.debug("================GetCode============================== from: %s" format sourceActor.path)
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      val socket = context.actorFor("/user/mainActor/socketActor")
      socket ! Request(httpGet, GetCode(path, sourceActor))
    }
  }
}

object CodeActor {

  case class GetCode(path: String, sourceActor: ActorRef)

  case class GetCodeSuccess(code: String)

  case class GetCodeFailure()

}

