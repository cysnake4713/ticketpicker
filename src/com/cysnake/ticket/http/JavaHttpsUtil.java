package com.cysnake.ticket.http;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/14/13
 * Time: 4:43 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */
public class JavaHttpsUtil {

    public static HttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");

// set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                System.out.println("getAcceptedIssuers =============");
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
                System.out.println("checkClientTrusted =============");
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
                System.out.println("checkServerTrusted =============");
            }
        }}, new SecureRandom());

        SSLSocketFactory sf = new SSLSocketFactory(sslContext);
        Scheme httpsScheme = new Scheme("https", 443, sf);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(httpsScheme);
        Scheme httpScheme = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
        schemeRegistry.register(httpScheme);

// apache HttpClient version >4.2 should use BasicClientConnectionManager
        ClientConnectionManager cm = new BasicClientConnectionManager(schemeRegistry);
        DefaultHttpClient hc = new DefaultHttpClient(cm);
//        HttpHost proxy = new HttpHost("127.0.0.1", 8087);
//        hc.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        return hc;
    }
}
