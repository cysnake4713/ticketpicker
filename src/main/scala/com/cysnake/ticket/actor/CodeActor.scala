package com.cysnake.ticket.actor

import akka.actor.{ActorRef, ActorLogging, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.apache.http.HttpStatus
import akka.pattern.ask
import akka.util.Timeout
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


  implicit val timeout = Timeout(10 seconds)

  import com.cysnake.ticket.actor.SocketActor._


  def receive = {
    case GetCode(path: String, sourceActor) => {

      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]

      val socket = context.actorFor("../socketActor")
      log.debug(self + "send request to socketActor")
      (socket ? Request(httpGet)).mapTo[Response] onSuccess {
        case Response(response) => {
          log.debug("response status: " + response.getStatusLine)
          if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
            val entity = response.getEntity
            val stream = entity.getContent
            CodeDialog.start(entity.getContent, sourceActor)
            EntityUtils.consume(entity)
            stream.close()
            httpGet.releaseConnection()
          } else {
            //TODO
          }
        }
      }

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
        this.dispose()
      }
    }

    def start(is: InputStream, actor: ActorRef) {
      inputText.text = ""
      if (is != null)
        imageLabel.icon = new ImageIcon(ImageIO.read(is))
      thisActor = actor
      this.centerOnScreen()
      this.open()
    }
  }

}

object CodeActor {

  case class GetCode(path: String, sourceActor: ActorRef)

  case class ReturnCodeResult(code: String)

}

