package com.cysnake.ticket.actor

import akka.actor.{ActorRef, Props, Actor}
import com.cysnake.har.HarEntity
import org.apache.http.client.methods.HttpGet
import com.cysnake.ticket.ui.CodeFrame
import com.cysnake.ticket.http.HttpsUtil
import org.apache.http.util.EntityUtils
import org.apache.http.client.HttpClient
import org.apache.http.HttpResponse
import akka.dispatch.Future
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 1:52 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */


class GetCodeActor extends Actor {
  implicit val timeout = Timeout(10 seconds)

  import context._


  override def preStart() {
    super.preStart()
    println("codeactor prestart")
  }


  override def postStop() {
    super.postStop()
    println("code actor poststop")
  }

  def receive = {
    case GetCode => {
      val path = """d:\ticket\head\loginPassCode.do.har"""
      val har = new HarEntity(path)

      val httpGet = har.generateHttpRequest.asInstanceOf[HttpGet]
      val socket = context.actorFor("../socketActor")
      //      val response = socket ! Send(httpGet)

      val future: Future[Response] = ask(socket, Send(httpGet)).mapTo[Response]
      future.map(ele => ele match {
        case Response(response: HttpResponse) => {

          println("status: " + response.getStatusLine)
          val entity = response.getEntity
          val codeFrame = new CodeFrame
          val stream = entity.getContent
          codeFrame.setImage(entity.getContent, this)
          codeFrame.startup(Array.empty)
          EntityUtils.consume(entity)
          stream.close()
          httpGet.abort()

          httpGet.releaseConnection()
        }

      })
    }

    case ResultCode(codeText: String) => {
      println("your input is: " + codeText)
      context.actorFor("..") ! GetCodeRuselt(codeText)
      //      context.stop(self)
      //      val loginActor = context.actorOf(Props[LoginActor], "loginActor")
      //      loginActor ! LoginFirst(codeText, httpClient)
    }


  }

}


case class GetCode

case class ResultCode(code: String)
