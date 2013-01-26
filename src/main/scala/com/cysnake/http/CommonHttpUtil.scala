package com.cysnake.http

import org.apache.http.client.{CookieStore, HttpClient}
import javax.net.ssl.{X509TrustManager, TrustManager, SSLContext}
import java.security.SecureRandom
import java.security.cert.X509Certificate
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.scheme.{PlainSocketFactory, Scheme, SchemeRegistry}
import org.apache.http.params.{BasicHttpParams, HttpParams, HttpConnectionParams}
import org.apache.http.client.params.{CookiePolicy, ClientPNames}
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.impl.client.{BasicCookieStore, DefaultHttpClient}
import org.apache.http._
import org.apache.http.conn.params.ConnRoutePNames
import org.apache.http.client.entity.GzipDecompressingEntity
import protocol.HttpContext

/**
 * Created by matt.cai

 * User: cysnake4713
 * Date: 13-1-26
 * Time: 下午11:44
 *
 */
object CommonHttpUtil {
  private val CONNECT_TIMEOUT = 10000
  private val READ_TIMEOUT = 10000

  def getCustomHttpClient(withProxy: Boolean): HttpClient = {
    val sslContext: SSLContext = SSLContext.getInstance("SSL")
    sslContext.init(null, Array[TrustManager](new X509TrustManager {
      def getAcceptedIssuers: Array[X509Certificate] = null

      def checkClientTrusted(certs: Array[X509Certificate], authType: String) {}

      def checkServerTrusted(certs: Array[X509Certificate], authType: String) {}
    }), new SecureRandom)



    val sf: SSLSocketFactory = new SSLSocketFactory(sslContext)
    val schemeRegistry: SchemeRegistry = new SchemeRegistry
    val httpsScheme: Scheme = new Scheme("https", 443, sf)
    val httpScheme: Scheme = new Scheme("http", 80, PlainSocketFactory.getSocketFactory)
    schemeRegistry.register(httpsScheme)
    schemeRegistry.register(httpScheme)

    val params: HttpParams = new BasicHttpParams
    params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY)
    params.setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false)
    params.setIntParameter(ClientPNames.MAX_REDIRECTS, 100)

    HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT)
    HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT)
    val clientConManager: ClientConnectionManager = new PoolingClientConnectionManager(schemeRegistry)
    val httpClient = new DefaultHttpClient(clientConManager, params)
    if (withProxy) {
      val proxy: HttpHost = new HttpHost("127.0.0.1", 8087)
      httpClient.getParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy)
    }
    val cookieStore: CookieStore = new BasicCookieStore
    httpClient.setCookieStore(cookieStore)
    seInterceptor(httpClient)
    httpClient
  }

  private def seInterceptor(httpClient: DefaultHttpClient) {
    httpClient.addResponseInterceptor(new HttpResponseInterceptor {
      override def process(response: HttpResponse, context: HttpContext) {
        val entity = response.getEntity
        if (entity != null) {
          val ceHeader: Header = entity.getContentEncoding
          if (ceHeader != null) {
            val codecs = ceHeader.getElements
            for (codec <- codecs) {
              if (codec.getName.equalsIgnoreCase("gzip")) {
                response.setEntity(new GzipDecompressingEntity(response.getEntity))
              }
            }
          }
        }
      }
    })

  }

}