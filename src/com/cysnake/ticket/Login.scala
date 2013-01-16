package com.cysnake.ticket

import com.cysnake.har.HarEntity
import org.apache.http.params.CoreProtocolPNames
import org.apache.http.{NameValuePair, HttpVersion}
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.util.EntityUtils
import java.io.InputStreamReader
import org.json.{JSONObject, JSONTokener}
import org.apache.http.message.BasicNameValuePair
import java.util.ArrayList

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/16/13
 * Time: 6:06 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object Login {
  val httpClient = HttpsUtil.getHttpClient
  var rand: String = ""

  def getPassCode(path: String) {
    val har = new HarEntity(path)
    val httpGet = har.generateHttpRequest(httpClient).asInstanceOf[HttpGet]
    val response = httpClient.execute(httpGet)
    println("status: " + response.getStatusLine)
    val entity = response.getEntity
    entity.getContent
    //TODO
  }

  //  def getPage(path: String) {
  //    val har = new HarEntity(path)
  //    val httpGet = har.generateHttpRequest(httpClient).asInstanceOf[HttpGet]
  //
  //    val response = httpClient.execute(httpGet)
  //    println("status: " + response.getStatusLine)
  //    val entity = response.getEntity
  //    val CharsetPattern = """.*charset=(.*)""".r
  //    val charset = entity.getContentType.getValue match {
  //      case CharsetPattern(char) => char
  //      case _ =>
  //    }
  //    //    val json = new JSONObject(new JSONTokener(new InputStreamReader(entity.getContent, charset.asInstanceOf[String])))
  //    println(EntityUtils.toString(entity))
  //    EntityUtils.consume(entity)
  //    httpGet.releaseConnection()
  //
  //  }

  def login1(path: String) {
    val har = new HarEntity(path)
    val httpPost = har.generateHttpRequest(httpClient).asInstanceOf[HttpPost]

    val response = httpClient.execute(httpPost)

    println("status: " + response.getStatusLine)
    val entity = response.getEntity
    val CharsetPattern = """.*charset=(.*)""".r
    val charset = entity.getContentType.getValue match {
      case CharsetPattern(char) => char
      case _ =>
    }
    val json = new JSONObject(new JSONTokener(new InputStreamReader(entity.getContent, charset.asInstanceOf[String])))
    EntityUtils.consume(entity)
    httpPost.releaseConnection()
    rand = json.get("loginRand").asInstanceOf[String]
  }

  def login(path: String) = {

    val har = new HarEntity(path)
    val httpPost = har.generateHttpRequest(httpClient).asInstanceOf[HttpPost]

    //___________________________________________
    val formParams: ArrayList[NameValuePair] = new ArrayList[NameValuePair]
    formParams add new BasicNameValuePair("loginRand", rand)
    formParams add new BasicNameValuePair("refundLogin", "N")
    formParams add new BasicNameValuePair("refundFlag", "Y")
    formParams add new BasicNameValuePair("loginUser.user_name", "cysnake4713")
    formParams add new BasicNameValuePair("nameErrorFocus", "")
    formParams add new BasicNameValuePair("user.password", "36937004cys")
    formParams add new BasicNameValuePair("passwordErrorFocus", "")
    formParams add new BasicNameValuePair("randErrorFocus", "")
    //TODO


    formParams add new BasicNameValuePair("randCode", "")
    //___________________________

    val response = httpClient.execute(httpPost)
    println("status: " + response.getStatusLine)
    val entity = response.getEntity
    val CharsetPattern = """.*charset=(.*)""".r
    val charset = entity.getContentType.getValue match {
      case CharsetPattern(char) => char
      case _ =>
    }
    val json = new JSONObject(new JSONTokener(new InputStreamReader(entity.getContent, charset.asInstanceOf[String])))
    EntityUtils.toString(entity)
    EntityUtils.consume(entity)
    httpPost.releaseConnection()
    json.get("loginRand").asInstanceOf[String]
  }
}
