package com.cysnake.ticket.http



/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/17/13
 * Time: 2:38 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
object HttpsUtil {
  private val httpsUtil = JavaHttpsUtil.getHttpClient

  def getHttpClient = httpsUtil

}
