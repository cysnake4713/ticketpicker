package com.cysnake.ticket.ui

import scala.swing._
import event.EditDone
import java.io.InputStream
import javax.swing.ImageIcon
import javax.imageio.ImageIO
import akka.actor.ActorRef
import com.cysnake.ticket.actor.CodeActor.ReturnCodeResult

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 9:50 AM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class CodeFrame(val actor: ActorRef, val codeType: String) extends SimpleSwingApplication {


  var image: Image = null
  var flag = false

  def top: Frame = new MainFrame {
    title = "请输入验证码"

    val imageLabel = new Label {
      if (image != null)
        icon = new ImageIcon(image)
    }
    val inputText = new TextField {
      columns = 4
    }

    contents = new FlowPanel {
      contents += imageLabel
      contents += inputText
      //      focusable = true
      //      requestFocus()
    }

    listenTo(inputText)

    reactions += {
      case EditDone(`inputText`) => {
        if (!flag) {
          println("code Frame get EditDone message!")
          actor ! ReturnCodeResult(inputText.text)
          flag = true
          closeOperation()
        }
      }
    }

  }

  def setImage(is: InputStream) {
    image = ImageIO.read(is)
  }
}
