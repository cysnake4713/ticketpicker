//package com.cysnake.ticket.ui
//
//import scala.swing._
//import event.EditDone
//import java.io.InputStream
//import javax.swing.ImageIcon
//import javax.imageio.ImageIO
//import akka.actor.{Props, ActorSystem, ActorRef}
//import akka.dispatch.Await
//import com.typesafe.config.ConfigFactory
//import com.cysnake.ticket.actor.CodeActor.GetCodeSuccess
//import com.cysnake.ticket.actor.MainActor._
//import com.cysnake.ticket.actor.MainActor
//
///**
// * This code is written by matt.cai and if you want use it, feel free!
// * User: matt.cai
// * Date: 1/17/13
// * Time: 9:50 AM
// * if you have problem here, please contact me: cysnake4713@gmail.com
// */
//object CodeFrame extends SimpleSwingApplication {
//
//  def top: Frame = new MainFrame {
//
//
//  }
//
//  def showDialog(is: InputStream, thisActor: ActorRef) {
//    val codeDialog = new CodeDialog(is, thisActor)
//    codeDialog.centerOnScreen()
//  }
//
//
//
//  override def startup(args: Array[String]) {
//    super.startup(args)
//    println("start")
//    val system = ActorSystem("MySystem")
//    val mainActor = system.actorOf(Props[MainActor], name = "MainActor")
//    mainActor ! StartMain
//  }
//
//}
