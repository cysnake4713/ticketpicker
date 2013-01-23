package com.cysnake.http;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.KeyStore;
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

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

//    public static HttpClient getNewHttpClient() {
//        try {
//            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            trustStore.load(null, null);
//
//            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
//            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//
//
//            SchemeRegistry registry = new SchemeRegistry();
//            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//            registry.register(new Scheme("https", sf, 443));
//
////            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
//            ClientConnectionManager cm = new BasicClientConnectionManager(registry);
////            return new DefaultHttpClient(ccm, params);
//            return new DefaultHttpClient(cm);
//        } catch (Exception e) {
//            System.out.print("get http error!");
//            return new DefaultHttpClient();
//        }
//    }

    public static HttpClient getHttpClient() throws NoSuchAlgorithmException,
            KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");

        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
//                System.out.println("getAcceptedIssuers =============");
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
//                System.out.println("checkClientTrusted =============");
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
//                System.out.println("checkServerTrusted =============");
            }
        }}, new SecureRandom());

        SSLSocketFactory sf = new SSLSocketFactory(sslContext);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        Scheme httpsScheme = new Scheme("https", 443, sf);
        Scheme httpScheme = new Scheme("http", 80,
                PlainSocketFactory.getSocketFactory());
        schemeRegistry.register(httpsScheme);
        schemeRegistry.register(httpScheme);

        HttpParams params = new BasicHttpParams();
        params.setParameter(ClientPNames.COOKIE_POLICY,
                CookiePolicy.BROWSER_COMPATIBILITY);
        params.setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
        params.setIntParameter(ClientPNames.MAX_REDIRECTS, 100);
//		params.setParameter(ClientPNames.DEFAULT_HOST,new HttpHost("dynamic.12306.cn",80));

//		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//		HttpProtocolParams.setUseExpectContinue(params, false);

        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);

        // apache HttpClient version >4.2 should use
        // BasicClientConnectionManager
        // ClientConnectionManager cm = new BasicClientConnectionManager(
        // schemeRegistry);
        //TODO : which one is good??
//        ClientConnectionManager clientConManager = new ThreadSafeClientConnManager(
//                params, schemeRegistry);
        ClientConnectionManager clientConManager = new ThreadSafeClientConnManager( schemeRegistry);
        DefaultHttpClient hc = new DefaultHttpClient(clientConManager, params);


        HttpHost proxy = new HttpHost("127.0.0.1", 8087);
        hc.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        CookieStore cookieStore = new BasicCookieStore();
        hc.setCookieStore(cookieStore);
        return hc;
    }
}

