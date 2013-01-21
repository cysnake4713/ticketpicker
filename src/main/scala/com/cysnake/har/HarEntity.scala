package com.cysnake.har

import java.io.{FileInputStream, InputStreamReader}
import org.json.{JSONArray, JSONObject, JSONTokener}
import collection.mutable
import org.apache.http.{HttpVersion, HeaderElement, Header}
import org.apache.http.params.CoreProtocolPNames
import org.apache.http.client.methods.{HttpRequestBase, HttpGet, HttpPost}
import org.apache.http.client.HttpClient
import scala.io.Source

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/16/13
 * Time: 4:21 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
class HarEntity {
  private var jsonObject: JSONObject = null

  def this(filePath: String) {
    this
    val file = Source.fromURL(getClass.getResource(filePath))
    //    val file = new FileInputStream(filePath)

    val token = new JSONTokener(file.reader())
    jsonObject = new JSONObject(token)
    file.close()
  }

  def getUrl: String = {
    val result = jsonObject.get("request").asInstanceOf[JSONObject].get("url").asInstanceOf[String]
    println("request url:" + result)
    result
  }

  def getMethod: String = {
    val method = jsonObject.get("request").asInstanceOf[JSONObject].get("method").asInstanceOf[String]
    println("requst method:" + method)
    method
  }

  def getHeaders: Array[Header] = {
    jsonObject.get("request").asInstanceOf[JSONObject].get("headers")
    val headers = jsonObject.get("request").asInstanceOf[JSONObject].get("headers").asInstanceOf[JSONArray]
    var headerBuilder = mutable.ArrayBuilder.make[Header]
    for (i <- 0 to headers.length - 1) {
      val headerJson = headers.getJSONObject(i)
      if ("Content-Length".equalsIgnoreCase(headerJson.get("name").toString) &&
        "Cookie".equalsIgnoreCase(headerJson.get("name").toString))
        headerBuilder += new HeaderImpl(headerJson.get("name").toString, headerJson.get("value").toString)
    }
    headerBuilder.result()
  }

  def getHttpVersion: String = {
    val version = jsonObject.get("request").asInstanceOf[JSONObject].get("httpVersion").asInstanceOf[String]
    println("request version:" + version)
    version
  }

  def generateHttpRequest: HttpRequestBase = {
    //    val pattern11 = """.*1.1""".r
    //TODO
    //    getHttpVersion match {
    //      case pattern11(test) => httpClient.getParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
    //        HttpVersion.HTTP_1_1)
    //      case _ => println("httpversion no match, use default!")
    //    }


    val httpRequest = getMethod.toUpperCase match {
      case "POST" => {
        val httpPost = new HttpPost(getUrl)

        httpPost.setHeaders(getHeaders)
        httpPost
      }
      case "GET" => {
        val httpGet = new HttpGet(getUrl)
        httpGet.setHeaders(getHeaders)
        httpGet
      }
      case _ => {
        println("http request method match error!exit")
        System.exit(0)
      }
    }
    httpRequest.asInstanceOf[HttpRequestBase]
  }

  class HeaderImpl(val name: String, val value: String) extends Header {

    def getName: String = name

    def getValue: String = value

    def getElements: Array[HeaderElement] = null
  }

}
