package com.cysnake.ticket.ui

import scala.swing._
import event.{EditDone, Key, KeyPressed}
import java.io.InputStream
import javax.swing.ImageIcon
import javax.imageio.ImageIO
import com.cysnake.ticket.actor.ResultCode
import akka.actor.{ActorContext, ActorSystem}

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 9:50 AM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class CodeFrame(val context: ActorContext) extends SimpleSwingApplication {
  var image: Image = null

  def top: Frame = new MainFrame {
    title = "请输入验证码"

    val imageLabel = new Label {
      if (image != null)
        icon = new ImageIcon(image)
    }
    val inputText = new TextField {
      columns = 4
    }

    //    val label = new Label {
    //      text = "asdfasdf"
    //    }
    contents = new FlowPanel {
      contents += imageLabel
      contents += inputText
      //      focusable = true
      //      requestFocus
    }

    listenTo(inputText)

    reactions += {
      case EditDone(`inputText`) => {
        println("editDone")
        val codeActor = context.actorFor("../getCodeActor")
        if (codeActor != null) {
          codeActor ! ResultCode(inputText.text)
        }
        closeOperation()
      }
    }

  }

  def setImage(is: InputStream) {
    image = ImageIO.read(is)
  }
}
