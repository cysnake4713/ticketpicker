package com.cysnake.ticket

import java.util.Properties
import java.io._
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.net.URI

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/14/13
 * Time: 2:28 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object TestMain {
  def main(args: Array[String]) {
    val props = prepareProperty(args(0))







    var httpclient = HttpsUtil.getHttpClient
//    InsecureHttpClientFactory.configureSSLHandling(httpclient)
    val httpPost = new HttpPost("https://dynamic.12306.cn/otsweb/loginAction.do?method=loginAysnSuggest")
    httpPost.setHeader("Origin", "https://dynamic.12306.cn")
    httpPost.setHeader("Host", "https://dynamic.12306.cn")
    httpPost.setHeader("Referer", "https://dynamic.12306.cn/otsweb/loginAction.do?method=init")
    val response = httpclient.execute(httpPost)
    println("status: " + response.getStatusLine)
    val entity = response.getEntity
    //    val bf = new BufferedReader(new InputStreamReader(entity.getContent))

    println("content: " + scala.io.Source.fromInputStream(entity.getContent).getLines().mkString("\n"))
    EntityUtils.consume(entity)
    httpPost.releaseConnection()
  }

  private def prepareProperty(path: String): Properties = {
    val props = new Properties()
    val in = new BufferedInputStream(new FileInputStream(path + "app.properties"))
    props.load(in)
    props
  }




}
