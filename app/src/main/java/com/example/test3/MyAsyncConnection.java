package com.example.test3;

import android.os.StrictMode;
import android.util.Log;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Factory;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.impl.bootstrap.AsyncRequesterBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

public class MyAsyncConnection implements Runnable{

    SSLContext ctx=null;
    BasicHttpContext basicHttpContext;
    MainActivity main;
    PoolingAsyncClientConnectionManager asyncConnMgr;
    String TAG="TLS13";
    AsyncConnectionEndpoint asyncConn;
    TlsStrategy tlsStrategy;
    CloseableHttpAsyncClient client;

    public MyAsyncConnection(MainActivity main) {
        this.main=main;
/*        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }*/
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
 /*               .setTlsDetailsFactory(new Factory<SSLEngine, TlsDetails>() {
                    @Override
                    public TlsDetails create(final SSLEngine sslEngine) {
                        return new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol());
                    }
                })*/
                .build();
        this.asyncConnMgr = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(tlsStrategy)
                .build();
        client= HttpAsyncClients.custom()
                .setConnectionManager(asyncConnMgr)
                .build();
//        client.start();
        basicHttpContext=new BasicHttpContext();
        main.login_state=MainActivity.CONNECT_REQUIRED;
    }

    public void asyncConnect(String hostname, int port) {
//        main.host = new HttpHost("https", hostname, port);
//        final HttpClientContext clientContext = HttpClientContext.create();



        MyCloseableHttpAsyncClient myClient=new MyCloseableHttpAsyncClient(main, hostname, port, client);
        new Thread(myClient).run();
        try {
            myClient.future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG+ " asyncConnect",e.getMessage());
        }
    }

    public void asyncDisconnect()
    {
        asyncConnMgr.release(asyncConn, null, TimeValue.ofMilliseconds(10));
        try {
            asyncConn.close();
        } catch (IOException e) {
        }
        main.connected=false;
        main.homeViewModel.postConnected(main.connected);
    }

    @Override
    public void run() {

    }
}
