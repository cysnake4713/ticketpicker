package com.cysnake.ticket;

/**
 * This code is written by matt.cai and if you want use it, feel free!
 * User: matt.cai
 * Date: 1/14/13
 * Time: 4:22 PM
 * if you have problem here, please contact me: cysnake4713@gmail.com
 */

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.logging.Logger;

import org.apache.http.client.CookieStore;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * <p>Sample factory for building a HttpClient that configures a HttpClient
 * instance to store cookies and to accept SSLcertificates without HostName validation.</p>
 * <p>You obviously should not use this class in production, but it may come handy when
 * developing with internal Servers using self-signed certificates.</p>
 */
public class InsecureHttpClientFactory {
    protected Logger log = Logger.getLogger("factory");
//    public DefaultHttpClient buildHttpClient() {
//        DefaultHttpClient hc = new DefaultHttpClient();
////        configureProxy();
////        configureCookieStore();
//        configureSSLHandling();
//        return hc;
//    }

//    private void configureProxy() {
//        HttpHost proxy = new HttpHost("proxy.example.org", 3182);
//        hc.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
//    }

//    private void configureCookieStore() {
//        CookieStore cStore = new BasicCookieStore();
//        hc.setCookieStore(cStore);
//    }

    static public void configureSSLHandling(DefaultHttpClient hc) {
        Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
        SSLSocketFactory sf = buildSSLSocketFactory();
        Scheme https = new Scheme("https", 443, sf);
        SchemeRegistry sr = hc.getConnectionManager().getSchemeRegistry();
        sr.register(http);
        sr.register(https);
    }

    static private SSLSocketFactory buildSSLSocketFactory() {
        TrustStrategy ts = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true; // heck yea!
            }
        };

        SSLSocketFactory sf = null;


			/* build socket factory with hostname verification turned off. */
        try {
            sf = new SSLSocketFactory(ts, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (KeyManagementException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (KeyStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return sf;
    }

}
