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

    MyAsyncConnectionService service;
    final String TAG="TLS13";
    public final static int CMD_DISCONNECT=0;
    public final static int CMD_CONNECT=1;
    public final static int CMD_LOGIN=1;
    final HttpClientContext clientContext = HttpClientContext.create();


    public MyAsyncConnection(MyAsyncConnectionService service) {
        this.service=service;
    }

//        main.host = new HttpHost("https", hostname, port);
//        final HttpClientContext clientContext = HttpClientContext.create();



    final FutureCallback<SimpleHttpResponse> futureCallback=new FutureCallback<SimpleHttpResponse>()
    {

        @Override
        public void completed(final SimpleHttpResponse response) {
            Log.i(TAG+ " asyncConnect", new StatusLine(response).toString());
            final SSLSession sslSession = clientContext.getSSLSession();
            if (sslSession != null) {
                Log.i(TAG+ " asyncConnect","SSL protocol " + sslSession.getProtocol());
                Log.i(TAG+ " asyncConnect","SSL cipher suite " + sslSession.getCipherSuite());
                Log.i(TAG+ " asyncConnect","Connected!");
                service.main.connected=true;
                service.main.login_state=MainActivity.BASIC_LOGIN_REQUIRED;
                service.main.homeViewModel.postConnected(service.main.connected);
            }
            Log.i(TAG+ " asyncConnect",response.getBody().toString());
        }

        @Override
        public void failed(final Exception ex) {
            service.main.connected=false;
            service.main.homeViewModel.postConnected(service.main.connected);
            Log.e(TAG+ " asyncConnect", ex.getMessage());
        }

        @Override
        public void cancelled() {
            service.main.connected=false;
            service.main.homeViewModel.postConnected(service.main.connected);
            Log.e(TAG+ " asyncConnect","Canceled");
        }
    };

    @Override
    public void run() {
        Log.i(TAG+ " MyAsyncConnection","Thread id="+Thread.currentThread().getId());
        switch (service.cmd) {
            case CMD_CONNECT:
                final HttpHost host = new HttpHost("https", service.hostname, service.port);
                final SimpleHttpRequest request = SimpleHttpRequests.get(host, "/login");
                final Future<SimpleHttpResponse> future = service.myAsyncConnectionClient.client.execute(
                        SimpleRequestProducer.create(request),
                        SimpleResponseConsumer.create(),
                        clientContext,
                        futureCallback
                );
                try {
                    future.get();
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
            case CMD_DISCONNECT:
                try {
                    service.myAsyncConnectionClient.client.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
        }
    }
}
