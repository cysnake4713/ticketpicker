package com.cysnake.ticket.actor

import akka.actor.{ActorSystem, Props, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet
import com.cysnake.ticket.ui.CodeFrame
import com.cysnake.ticket.http.HttpsUtil
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask
import akka.dispatch.Await

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 1:52 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object GetCodeActor {
  implicit val timeout = Timeout(60000)
  val system = ActorSystem("MySystem")
  //  lazy val default = {
  val getCodeActor = system.actorOf(Props[GetCodeActor], name = "getCodeActor")

  //  getCodeActor

  //  }

  def getCode(path: String): String = {
    val future = (getCodeActor ? GetCode(path))
    val result = Await.result(future, timeout.duration).asInstanceOf[String]
    println("akka result:" + result)
    result
  }
}

class GetCodeActor extends Actor {

  import context._

  def receive = {
    case GetCode(path: String) => {
      val httpClient = HttpsUtil.getHttpClient
      val har = new HarEntity(path)
      val httpGet = har.generateHttpRequest(httpClient).asInstanceOf[HttpGet]
//      val response = httpClient.execute(httpGet)
//      println("status: " + response.getStatusLine)
//      val entity = response.getEntity
//      CodeFrame.setImage(entity.getContent, this)
      CodeFrame.setImage(null, this)
      CodeFrame.startup(Array.empty)
    }
  }

}


case class GetCode(path: String)

case class ResultCode(code: String)