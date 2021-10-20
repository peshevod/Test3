package com.example.test3;

import android.os.StrictMode;
import android.util.Log;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
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
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class MyAsyncConnection {

    SSLContext ctx=null;
    BasicHttpContext basicHttpContext;
    MainActivity main;
    PoolingAsyncClientConnectionManager asyncConnMgr;
    String TAG="TLS13";
    AsyncConnectionEndpoint asyncConn;
    TlsStrategy tlsStrategy;

    public MyAsyncConnection(MainActivity main) {
        this.main=main;
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
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
        basicHttpContext=new BasicHttpContext();
        main.login_state=MainActivity.CONNECT_REQUIRED;
    }

    public void asyncConnect(String hostname, int port) {
        main.host = new HttpHost("https", hostname, port);
        HttpRoute route = new HttpRoute(main.host);

        FutureCallback<AsyncConnectionEndpoint> fcb_connect = new FutureCallback<AsyncConnectionEndpoint>() {

            @Override
            public void completed(AsyncConnectionEndpoint result) {
                main.connected = true;
                main.homeViewModel.postConnected(main.connected);
                Log.i("TLS13", "Connected!");
                asyncConnMgr.release(result, null, TimeValue.ofHours(1));
                main.login_state = MainActivity.BASIC_LOGIN_REQUIRED;
            }

            @Override
            public void failed(Exception ex) {
                main.connected = false;
                main.homeViewModel.postConnected(main.connected);
                Log.e(TAG+" fcb_connect", ex.getMessage());
            }

            @Override
            public void cancelled() {
                main.connected = false;
                main.homeViewModel.postConnected(main.connected);
                Log.e(TAG+" fcb_connect", "Connection cancelled");
            }
        };


        FutureCallback<AsyncConnectionEndpoint> fcb_lease = new FutureCallback<AsyncConnectionEndpoint>() {
            @Override
            public void completed(AsyncConnectionEndpoint result) {
                asyncConn = result;
                if (!asyncConn.isConnected()) {
                    Log.i(TAG, "No connection yet, connecting...");
/*                    asyncConnMgr.connect(asyncConn, new ConnectionInitiator(){
                        public Future<IOSession> connect(NamedEndpoint remoteEndpoint, SocketAddress remoteAddress, SocketAddress localAddress, Timeout timeout, Object attachment, FutureCallback<IOSession> callback) {
                            return null;
                        }
                    }, Timeout.ofMilliseconds(5000), null, new BasicHttpContext(), fcb_connect);*/
                    HttpAsyncRequester ci= AsyncRequesterBootstrap
                            .bootstrap()
/*                            .setTlsStrategy(tlsStrategy)
                           .setConnectionReuseStrategy(new ConnectionReuseStrategy() {
                                @Override
                                public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
                                    return true;
                                }
                            })
                            .setIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(3).build())*/
                            .create();
//                    DefaultConnectingIOReactor ci=new DefaultConnectingIOReactor(null);
                    ci.start();
                    asyncConnMgr.connect(asyncConn, ci , Timeout.ofMilliseconds(5000),
                            null, new BasicHttpContext(), fcb_connect);
                }
                else
                {
                    main.connected = true;
                    main.homeViewModel.postConnected(main.connected);
                    Log.i(TAG, "Connection is already opened");
                }
            }

            @Override
            public void failed(Exception ex) {
                Log.e(TAG, ex.getMessage());
                main.connected = false;
                main.homeViewModel.postConnected(main.connected);
                return;
            }

            @Override
            public void cancelled() {
                main.connected = false;
                main.homeViewModel.postConnected(main.connected);
                Log.e(TAG+" fcb_lease", "Connection cancelled");
            }
        };
        asyncConnMgr.lease("1", route, null, Timeout.ofMilliseconds(3000), fcb_lease);
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
}
