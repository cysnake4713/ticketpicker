package com.cysnake.ticket

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import xml.XML

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/24/13
 * Time: 4:01 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class TestRegx extends WordSpec with MustMatchers with BeforeAndAfterAll {

  var target: String = null
  var tokenContext: String = null

  override protected def beforeAll() {
    super.beforeAll()
    target = scala.io.Source.fromURL(getClass.getResource("/testfile/test.html"), "utf-8").mkString("")
    tokenContext = scala.io.Source.fromURL(getClass.getResource("/testfile/test-token.html"), "utf-8").mkString("")
  }

  "Target" must {
    "read success" in {
      println("length:" + target.length)
      assert(target.length > 0)
    }
    "spilt correct" in {
      val regx = """<span()"""
      val result = target.split(regx)
      var trainLine = ""
      val xml = XML.load(getClass.getResource("/ticket.xml"))
      val trainNum = (xml \ "ticket" \ "train").text
      println("train num: " + trainNum)
      for (re <- result) {
        val regxIsThisLine = (""".*onmouseout=\'onStopOut\(\)\'\>""" + trainNum + """\<\/span\>.*""").r
        if (!regxIsThisLine.findFirstMatchIn(re).isEmpty) {
          trainLine = re
        }
      }
      println("match line is :" + trainLine)
      assert(trainLine != "")

      val regx2 = """.*javascript:getSelected\(\'(.*)\'\).*""".r
      val temp = trainLine match {
        case regx2(value) => value
      }
      //      for (va <- temp.split("#")) {
      //        println("final: " + va)
      //      }
    }
  }


  "Token" must {
    "read success" in {
      println("length:" + tokenContext.length)
      assert(tokenContext.length > 0)
    }

    "regx token" in {
      val tokenRegx = """.*name\=\"org\.apache\.struts\.taglib\.html\.TOKEN\"\s*value\=\"(\w{32})\"\>.*""".r
      val token = tokenContext match {
        case tokenRegx(value) => value
        case _ => ""
      }
      println("token is: " + token)
      assert(token != "")
    }

    "regx leftticket" in {
      val leftTicketRegx = """.*name=\"leftTicketStr\"\s*id=\"left\_ticket\"\s*value\=\"(\w{30})\".*""".r
      val leftTicket = tokenContext match {
        case leftTicketRegx(value) => value
        case _ => ""
      }
      println(("left ticket: " + leftTicket))
      assert(leftTicket != "")
    }

    "regx trainNum" in {
      val trainNumRegx = """.*orderRequest\.train\_no\"\s*value\=\"(\w*)\".*""".r
      val trainNum = tokenContext match {
        case trainNumRegx(value) => value
        case _ => ""
      }
      println("train num is: " + trainNum)
      assert(trainNum != "")
    }

  }
}
