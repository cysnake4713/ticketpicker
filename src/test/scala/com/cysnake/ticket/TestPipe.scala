package com.cysnake.ticket

import akka.testkit.TestKit
import akka.actor.{Props, Actor, ActorSystem, ActorRef}
import org.scalatest.{WordSpec, BeforeAndAfterAll}
import org.scalatest.matchers.MustMatchers
import akka.dispatch.Future
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.util.duration._
import com.cysnake.ticket.TestPipe.{TestActor, Start}
import akka.event.LoggingReceive

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/23/13
 * Time: 5:30 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object TestPipe {

  class TestActor extends Actor {

    import context.system

    override def receive: Receive = LoggingReceive {
      case Start => {
        val a: Future[Result] = for {
          b <- Future("hello" + "world")
        } yield Result(b)
        println("steststasd")
        println("a is " + a)
        a pipeTo self
        //        self ! "adf"
      }
      case Future => {
        println("get result")
      }


      case e: String => {
        println(e)
      }
    }
  }

  case class Start()

  case class Result(code: String)

}


class TestPipe(_system: ActorSystem) extends TestKit(_system) with WordSpec
with MustMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("thisSystem"))

  var mainActor: ActorRef = null

  override protected def beforeAll() {
    super.beforeAll()
    //    mainActor = system.actorOf(Props[MainActor], name = "mainActor")
  }

  override def afterAll {
    system.shutdown()
  }


  "test pipeTo function" must {
    "pipeTo is" in {
      val testActor = system.actorOf(Props[TestActor])
      testActor ! Start
    }
  }
}
