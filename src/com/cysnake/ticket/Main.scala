package com.cysnake.ticket

import actor.GetCodeActor
import ui.CodeFrame
import java.net.URL

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/16/13
 * Time: 6:24 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object Main {
  def main(args: Array[String]) {
    GetCodeActor.getCode( """d:\ticket\head\loginPassCode.do.har""")
    //    Login.getPage("""d:\ticket\head\loginpage.har""")

    //    Login.login1("""d:\ticket\head\loginAction.do.har""")
    //    Login.login("""d:\ticket\head\loginAction.do2.har""")
  }
}
