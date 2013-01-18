package com.cysnake.ticket

import actor.{GetCode, GetCodeActor}
import ui.CodeFrame
import java.net.URL
import akka.util.Timeout
import akka.actor.{Props, ActorSystem}
import akka.dispatch.Await

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/16/13
 * Time: 6:24 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */

object ActorBuilder {
  //  implicit val timeout = Timeout(600)
  var result: String = null
  val system = ActorSystem("MySystem")


  def start = {

    val getCodeActor = system.actorOf(Props[GetCodeActor], name = "getCodeActor")
    getCodeActor ! GetCode
  }
}


object Main {
  def main(args: Array[String]) {
    println("start")
    ActorBuilder.start
    //    Login.getPage("""d:\ticket\head\loginpage.har""")

    //    Login.login1("""d:\ticket\head\loginAction.do.har""")
    //    Login.login("""d:\ticket\head\loginAction.do2.har""")
  }
}
