package com.cysnake.ticket.ui

import swing._
import event.{EditDone, Key, KeyPressed}
import java.io.InputStream
import javax.swing.ImageIcon
import javax.imageio.ImageIO
import akka.actor.Actor
import com.cysnake.ticket.actor.ResultCode

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 9:50 AM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class CodeFrame extends SimpleSwingApplication {
  var image: Image = null
  var codeActor: Actor = null

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
        if (codeActor != null) {
          codeActor.self ! ResultCode(inputText.text)
        }
        closeOperation()
      }
    }

  }

  def setImage(is: InputStream, actor: Actor) {
    image = ImageIO.read(is)
    codeActor = actor
  }
}
