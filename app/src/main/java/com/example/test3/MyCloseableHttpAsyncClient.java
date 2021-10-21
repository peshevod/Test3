package com.example.test3;

import android.util.Log;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.StatusLine;

import java.util.concurrent.Future;

import javax.net.ssl.SSLSession;

public class MyCloseableHttpAsyncClient implements Runnable {

    String hostname;
    int port;
    CloseableHttpAsyncClient client;
    SimpleHttpRequest request;
    public Future<SimpleHttpResponse> future;
    HttpHost host;
    HttpClientContext clientContext;
    String TAG="TLS13";
    MainActivity main;

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
                main.connected=true;
                main.login_state=MainActivity.BASIC_LOGIN_REQUIRED;
                main.homeViewModel.postConnected(main.connected);
            }
            Log.i(TAG+ " asyncConnect",response.getBody().toString());
        }

        @Override
        public void failed(final Exception ex) {
            main.connected=false;
            main.homeViewModel.postConnected(main.connected);
            Log.e(TAG+ " asyncConnect", ex.getMessage());
        }

        @Override
        public void cancelled() {
            main.connected=false;
            main.homeViewModel.postConnected(main.connected);
            Log.e(TAG+ " asyncConnect","Canceled");
        }

    };
    public MyCloseableHttpAsyncClient(MainActivity main, String hostname, int port, CloseableHttpAsyncClient client)
    {
        this.main=main;
        this.hostname=hostname;
        this.port=port;
        this.client=client;
    }

   @Override
    public void run() {
       host = new HttpHost("https", hostname, port);
       request = SimpleHttpRequests.get(host,"/login");
       clientContext = HttpClientContext.create();
       client.start();
       future = client.execute(
               SimpleRequestProducer.create(request),
               SimpleResponseConsumer.create(),
               clientContext,
               futureCallback
       );
   }
}
