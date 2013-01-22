package com.cysnake.ticket.ui

import scala.swing._
import event.EditDone
import java.io.InputStream
import javax.swing.ImageIcon
import javax.imageio.ImageIO
import akka.actor.{Props, ActorSystem, ActorRef}
import com.cysnake.ticket.actor.CodeActor.ReturnCodeResult
import com.cysnake.ticket.actor.MainActor._
import com.cysnake.ticket.actor.MainActor

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 9:50 AM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object CodeFrame extends SimpleSwingApplication {

  def top: Frame = new MainFrame {


  }

  def showDialog(is: InputStream, thisActor: ActorRef) {
    val codeDialog = new CodeDialog(is, thisActor)
    codeDialog.centerOnScreen()
  }

  class CodeDialog(is: InputStream, thisActor: ActorRef) extends Dialog {
    title = "请输入验证码"
    val imageLabel = new Label {
      if (is != null)
        icon = new ImageIcon(ImageIO.read(is))
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
        println("code Frame get EditDone message!" + thisActor)
        if (thisActor != null) {
          thisActor ! ReturnCodeResult(inputText.text)
          CodeDialog.this.close()
        }
      }
    }
  }

  override def startup(args: Array[String]) {
    super.startup(args)
    println("start")
    val system = ActorSystem("MySystem")
    val mainActor = system.actorOf(Props[MainActor], name = "MainActor")
    mainActor ! StartMain
  }
}
