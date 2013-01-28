package com.cysnake.ticket.actor.ui

import akka.actor.ActorRef
import java.awt.Dimension
import javax.swing.ImageIcon
import swing.event.EditDone
import swing.{FlowPanel, TextField, Label, Dialog}
import com.cysnake.ticket.actor.CodeActor.GetCodeSuccess

object CodeDialog extends Dialog {
  title = "请输入验证码"
  val imageLabel = new Label
  private var thisActor: ActorRef = null
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