package com.cysnake.ticket

import http.HttpsUtil
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpHost
import org.apache.http.conn.params.ConnRoutePNames
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/16/13
 * Time: 2:59 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object test {
  def main(args: Array[String]) {
    //            System.setProperty("proxySet", "true");
    //            System.setProperty("proxyHost", "127.0.0.1");
    //            System.setProperty("proxyPort", "8087");
    val httpClient = HttpsUtil.getHttpClient
//    val proxy = new HttpHost("127.0.0.1", 8087)
//    httpClient.getParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy)
    val httpget = new HttpGet()
    httpget.setURI(new java.net.URI("https://www.12306.cn/mormhweb/kyfw/ypcx/"))

    val response = httpClient.execute(httpget)
    val entity = response.getEntity
    val content = EntityUtils.toString(entity, "utf8")
    System.out.println("is=" + content)
  }
}
