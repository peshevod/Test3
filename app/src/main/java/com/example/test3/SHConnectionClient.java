package com.example.test3;

import android.util.Log;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.ssl.TLS;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class SHConnectionClient {

    SSLContext ctx=null;
    public PoolingHttpClientConnectionManager connMgr;
    String TAG="TLS13 SHConnectionClient";
    SHConnectionService service;

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
        Log.i(TAG, " MySSLSocketFactory successfully created");
        //        DefaultHostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(null);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
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
        connMgr = new PoolingHttpClientConnectionManager(registry);
    }

}
