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

  override protected def beforeAll() {
    super.beforeAll()
    target = scala.io.Source.fromURL(getClass.getResource("/testfile/test.html"), "utf-8").mkString("")
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
        if (re.contains(trainNum)) {
          trainLine = re
        }
      }
      println("match line is :" + trainLine)
      assert(trainLine != "")

      val regx2 = """.*javascript:getSelected\(\'(.*)\'\).*""".r
      val temp = trainLine match {
        case regx2(key) => key
      }
      for (va <- temp.split("#")) {
        println("final: " + va)
      }
    }
  }
}
