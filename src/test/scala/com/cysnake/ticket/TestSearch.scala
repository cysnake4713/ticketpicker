package com.cysnake.ticket

import actor.SearchActor
import actor.SearchActor.Start
import akka.testkit.TestKit
import akka.actor.{Props, ActorSystem}
import org.scalatest.{WordSpec, BeforeAndAfterAll}
import org.scalatest.matchers.MustMatchers

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

  override def afterAll {
    system.shutdown()
  }

  "SearchActor" must {
    "start correct" in {
      val searchActor = system.actorOf(Props[SearchActor])
      searchActor ! Start
    }

  }
}
