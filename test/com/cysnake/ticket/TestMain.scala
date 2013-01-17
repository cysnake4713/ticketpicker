package com.cysnake.ticket

import http.{HttpsUtil, JavaHttpsUtil}
import java.util.Properties
import java.io._
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import java.net.URI
import org.json.{JSONObject, JSONTokener}
import org.apache.http.protocol.HTTP
import org.apache.http.params.CoreProtocolPNames
import org.apache.http.HttpVersion
import org.apache.http.entity.mime.Header
import com.cysnake.har.HarEntity

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/14/13
 * Time: 2:28 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object TestMain {
  def main(args: Array[String]) {
    //    System.setProperty("proxySet", "true")
    //    System.setProperty("proxyHost", "127.0.0.1")
    //    System.setProperty("proxyPort", "8087")
    val props = prepareProperty(args(0))
    val httpclient = HttpsUtil.getHttpClient
    //    InsecureHttpClientFactory.configureSSLHandling(httpclient)
    httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
      HttpVersion.HTTP_1_1)

    val har = new HarEntity( """d:\ticket\head\loginAction.do.har""")
    val httpPost = new HttpPost(har.getUrl)
    httpPost.setHeaders(har.getHeaders)
    val response = httpclient.execute(httpPost)
    println("status: " + response.getStatusLine)
    val entity = response.getEntity
    val CharsetPattern = """.*charset=(.*)""".r
    val charset = entity.getContentType.getValue match {
      case CharsetPattern(char) => char
      case _ =>
    }
    println("charset" + charset)

    println("len:" + entity.getContentLength)
    println("content type:" + entity.getContent)

    //    entity.getContentType.getValue
    if (charset.toString != "") {
      //       println("content: " + scala.io.Source.fromInputStream(entity.getContent, charset.toString).getLines().mkString("\n"))
      //      val json = new JSONObject(new JSONTokener(new InputStreamReader(entity.getContent(), HTTP.UTF_8)));
      val result = EntityUtils.toString(entity, charset.toString)
      println(result)
      //      println("content: " + scala.io.Source.fromInputStream(entity.getContent.charset).getLines().mkString("\n"))
    } else {
    }
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
