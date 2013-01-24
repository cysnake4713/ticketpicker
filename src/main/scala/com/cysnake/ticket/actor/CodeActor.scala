package com.cysnake.ticket.actor

import akka.actor.{ActorRef, ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.apache.http.HttpStatus
import akka.util.duration._
import scala.Predef._
import com.cysnake.ticket.actor.CodeActor.{ReturnCodeResult, GetCode}
import java.io.InputStream
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
          if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
            val entity = response.getEntity
            val stream = entity.getContent
            CodeDialog.start(entity.getContent, sourceActor)
            EntityUtils.consume(entity)
            stream.close()
            httpRequest.releaseConnection()
          } else {
            //TODO
          }
        }
      }
    }

    case GetCode(path: String, sourceActor) => {

      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      val socket = context.actorFor("/user/mainActor/socketActor")
      log.debug(self + "send request to socketActor")
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


    preferredSize = new Dimension(150, 60)
    contents = new FlowPanel {
      contents += imageLabel
      contents += inputText

      //      focusable = true
      //      requestFocus()
    }
    listenTo(inputText)

    reactions += {
      case EditDone(`inputText`) => {
        println("code Frame get EditDone message!" + thisActor)
        if (thisActor != null) {
          thisActor ! ReturnCodeResult(inputText.text)
          thisActor = null
        }
        this.close()
      }
    }

    def start(is: InputStream, actor: ActorRef) {
      inputText.text = ""
      if (is != null)
        try {
          imageLabel.icon = new ImageIcon(ImageIO.read(is))
        } catch {
          case e: Exception => log.error(e, "parse image error!!!------------" + actor)
        }
      thisActor = actor
      this.open()
    }
  }

}

object CodeActor {

  case class GetCode(path: String, sourceActor: ActorRef)

  case class ReturnCodeResult(code: String)

}

