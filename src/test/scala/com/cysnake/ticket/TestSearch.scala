package com.cysnake.ticket

import akka.testkit.TestKit
import akka.actor.{Actor, ActorSystem, ActorRef}
import org.scalatest.{WordSpec, BeforeAndAfterAll}
import org.scalatest.matchers.MustMatchers
import akka.dispatch.Future

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/23/13
 * Time: 5:30 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */

class TestSearch(_system: ActorSystem) extends TestKit(_system) with WordSpec
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

  //  "SearchActor" must {
  //    "start correct" in {
  //      val searchActor = system.actorFor("akka://thisSystem/user/mainActor/searchActor")
  //    }
  //  }

  //  "LoginActor" must {
  //    "get rand number" in {
  //            val loginActor = system.actorFor("akka://thisSystem/user/mainActor/loginActor")
  //            loginActor ! LoginFirst("")
  //      mainActor ! StartMain
  //    }
  //  }

}
