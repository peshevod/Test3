package com.example.test3;

import android.util.Log;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class SHConnectionClient {

    SSLContext ctx=null;
    public PoolingHttpClientConnectionManager connMgr;
    String TAG="TLS13 SHConnectionClient";
    SHConnectionService service;
    Registry<ConnectionSocketFactory> registry;
    public HttpRequestExecutor httpRequestExecutor;
    public CloseableHttpClient httpClient;
    public SHConnectionClient(SHConnectionService service)
    {
        this.service=service;
        try {
            ctx = SSLContext.getInstance(String.valueOf(TLS.V_1_3));
            ctx.init(null, null, null);
            ctx.createSSLEngine();
         } catch (KeyManagementException | NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        }
        SSLConnectionSocketFactory sslf=SSLConnectionSocketFactoryBuilder
                .create()
                .setCiphers("TLS_AES_128_GCM_SHA256")
                .setHostnameVerifier(new DefaultHostnameVerifier(null))
                .setTlsVersions(TLS.V_1_3)
                .build();
        Log.i(TAG, " MySSLSocketFactory successfully created");
        //        DefaultHostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(null);
        registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https",SSLConnectionSocketFactoryBuilder
                        .create()
                        .setSslContext(ctx)
                        .setTlsVersions(TLS.V_1_3)
                        .setCiphers(new String[]{"TLS_AES_128_GCM_SHA256"})
                        .setHostnameVerifier(new DefaultHostnameVerifier(null))
                        .build()
               )
                .build();
//        connMgr = new PoolingHttpClientConnectionManager(registry);
        connMgr=PoolingHttpClientConnectionManagerBuilder
                .create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder
                        .create()
                        .setCiphers("TLS_AES_128_GCM_SHA256")
                        .setHostnameVerifier(new DefaultHostnameVerifier(null))
                        .setTlsVersions(TLS.V_1_3)
                        .build())
                .setConnPoolPolicy(PoolReusePolicy.FIFO)
                .setMaxConnPerRoute(2)
                .setConnectionTimeToLive(TimeValue.ofSeconds(30))
                .build();
        httpRequestExecutor=new HttpRequestExecutor(new ConnectionReuseStrategy() {
            @Override
            public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
                Log.i(TAG,"Query to reuse");
                return true;
            }
        });
        httpClient= HttpClientBuilder.create()
                .setConnectionManager(connMgr)
//                .setDefaultCredentialsProvider(provider)
//                .setRequestExecutor(httpRequestExecutor)
                .setConnectionReuseStrategy(new ConnectionReuseStrategy() {
                    @Override
                    public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
                        Log.i(TAG,"Query to reuse");
                        return true;
                    }
                })
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofSeconds(3))
                        .setConnectTimeout(Timeout.ofSeconds(10))
                        .setResponseTimeout(Timeout.ofSeconds(15))
                        .build())
                .build();
    }

}
