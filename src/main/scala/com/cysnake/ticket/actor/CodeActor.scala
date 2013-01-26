package com.cysnake.ticket.actor

import akka.actor.{ActorRef, ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.HttpStatus
import scala.Predef._
import com.cysnake.ticket.actor.CodeActor.{GetCodeFailure, GetCodeSuccess, GetCode}
import swing.{FlowPanel, TextField, Label, Dialog}
import javax.swing.ImageIcon
import javax.imageio.ImageIO
import swing.event.EditDone
import java.awt.Dimension

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

  object CodeDialog extends Dialog {
    title = "请输入验证码"
    val imageLabel = new Label
    var thisActor: ActorRef = null
    val inputText = new TextField {
      columns = 4
    }


    preferredSize = new Dimension(200, 100)
    contents = new FlowPanel {
      contents += imageLabel
      contents += inputText
      //      focusable = true
      //      requestFocus()
    }
    listenTo(inputText)

    reactions += {
      case EditDone(`inputText`) => {
        if (thisActor != null) {
          //          println("code Frame get EditDone message!" + thisActor)
          thisActor ! GetCodeSuccess(inputText.text)
          thisActor = null
        }
        this.close()
      }
    }

    def start(imageIcon: ImageIcon, actor: ActorRef) {
      inputText.text = ""
      if (imageIcon != null)
        imageLabel.icon = imageIcon
      thisActor = actor
      this.open()
    }
  }

}

object CodeActor {

  case class GetCode(path: String, sourceActor: ActorRef)

  case class GetCodeSuccess(code: String)

  case class GetCodeFailure()

}

