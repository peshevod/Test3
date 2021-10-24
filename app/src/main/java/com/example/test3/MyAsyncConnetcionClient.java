package com.example.test3;

import android.os.IBinder;
import android.util.Log;

import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class MyAsyncConnetcionClient implements Runnable{

    public CloseableHttpAsyncClient client;
    MyAsyncConnectionService service;
    SSLContext ctx=null;
    PoolingAsyncClientConnectionManager asyncConnMgr;
    String TAG="TLS13 Client";
    TlsStrategy tlsStrategy;

    public MyAsyncConnetcionClient(MyAsyncConnectionService service)
    {
        this.service=service;
    }

    @Override
    public void run() {
        Log.i(TAG+ " MyAsyncConnectionClient","Thread id="+Thread.currentThread().getId());
        try {
            ctx = SSLContext.getInstance("TLSv1.3");
            ctx.init(null, null, null);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.i(TAG, " MySSLSocketFactory successfully created");
        tlsStrategy = ClientTlsStrategyBuilder.create()
                .setSslContext(ctx)
                .setTlsVersions("TLSv1.3")
                .setCiphers("TLS_AES_128_GCM_SHA256")
                .setHostnameVerifier(new DefaultHostnameVerifier(null))
                .build();
        asyncConnMgr = PoolingAsyncClientConnectionManagerBuilder.create()
                .setDnsResolver(SystemDefaultDnsResolver.INSTANCE)
                .setTlsStrategy(tlsStrategy)
                .build();
        client= HttpAsyncClients.custom()
                .setConnectionManager(asyncConnMgr)
                .build();
        client.start();
    }
}
