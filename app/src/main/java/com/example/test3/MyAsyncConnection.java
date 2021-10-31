package com.example.test3;

import android.util.Log;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.DefaultThreadFactory;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.AsyncRequesterBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.impl.nio.ClientHttp1IOEventHandlerFactory;
import org.apache.hc.core5.http.impl.nio.ClientHttp1StreamDuplexerFactory;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.command.ShutdownCommand;
import org.apache.hc.core5.http.protocol.DefaultHttpProcessor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.protocol.H2RequestConnControl;
import org.apache.hc.core5.http2.protocol.H2RequestContent;
import org.apache.hc.core5.http2.protocol.H2RequestTargetHost;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.net.ssl.SSLSession;

public class MyAsyncConnection implements Runnable{

    MyAsyncConnectionService service;
    final String TAG="TLS13 MyAsyncConnection";
    public final static int CMD_DISCONNECT=0;
    public final static int CMD_CONNECT=1;
    public final static int CMD_LOGIN=1;
    HttpClientContext clientContext = HttpClientContext.create();
    AsyncConnectionEndpoint asyncConnectionEndpoint;

    public MyAsyncConnection(MyAsyncConnectionService service) {
        this.service=service;
    }

    final FutureCallback<SimpleHttpResponse> futureCallback=new FutureCallback<SimpleHttpResponse>()
    {

        @Override
        public void completed(final SimpleHttpResponse response) {
            Log.i(TAG+ " asyncConnect", new StatusLine(response).toString());
            final SSLSession sslSession = clientContext.getSSLSession();
            if (sslSession != null) {
                Log.i(TAG,"SSL protocol " + sslSession.getProtocol());
                Log.i(TAG,"SSL cipher suite " + sslSession.getCipherSuite());
                Log.i(TAG,"Connected!");
                service.main.connected=true;
                service.main.login_state=MainActivity.BASIC_LOGIN_REQUIRED;
                service.main.homeViewModel.postConnected(service.main.connected);
            }
            Log.i(TAG,response.getBody().toString());
        }

        @Override
        public void failed(final Exception ex) {
            service.main.connected=false;
            service.main.homeViewModel.postConnected(service.main.connected);
            Log.e(TAG, ex.getMessage());
        }

        @Override
        public void cancelled() {
            service.main.connected=false;
            service.main.homeViewModel.postConnected(service.main.connected);
            Log.e(TAG,"Canceled");
        }
    };

    @Override
    public void run() {
        Log.i(TAG,"Thread id="+Thread.currentThread().getId());
        switch (service.cmd) {
            case CMD_CONNECT:
                HttpHost host=new HttpHost("https", service.hostname, service.port);
                HttpRoute route=new HttpRoute(host,null,true);
                asyncConnectionEndpoint=null;
                Future<AsyncConnectionEndpoint> leaseFuture=service.myAsyncConnectionClient.asyncConnMgr.lease(null, route, null, Timeout.ofSeconds(5), new FutureCallback<AsyncConnectionEndpoint>() {
                    @Override
                    public void completed(AsyncConnectionEndpoint result) {
                        Log.i(TAG," Lease Success");
                        asyncConnectionEndpoint=result;
                     }

                    @Override
                    public void failed(Exception ex) {
                        Log.e(TAG," Lease Failed "+ex.getMessage());
                    }

                    @Override
                    public void cancelled() {
                        Log.e(TAG," Lease Cancelled");
                    }
                });
                try {
                    leaseFuture.get();
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG,"lease future get "+e.getMessage());
                    return;
                }
                if(asyncConnectionEndpoint==null)
                {
                    Log.e(TAG,"lease future get is null");
                    return;
                }
                Log.i(TAG," Lease Success trying connect to "+service.hostname+":"+service.port);
                DefaultConnectingIOReactor defaultConnectingIOReactor=
                new DefaultConnectingIOReactor(
                        new MyIOHandlerFactory()
/*                        new ClientHttp1IOEventHandlerFactory(
                                new ClientHttp1StreamDuplexerFactory(
                                        HttpProcessors.client(),null,null
                               ),new MyClientTlsStrategy(),Timeout.ofSeconds(5)
//                            ),null,Timeout.ofSeconds(5)
                        )*/
                );
                defaultConnectingIOReactor.start();
/*               HttpAsyncRequester httpAsyncRequester=
                        AsyncRequesterBootstrap.bootstrap()
//                                .setTlsStrategy(new EmptyClientTlsStrategy())
                                .setTlsStrategy(new MyClientTlsStrategy())
//                                .setTlsStrategy(service.myAsyncConnectionClient.tlsStrategy)
                                .setTlsHandshakeTimeout(Timeout.ofSeconds(5))
                                .setIOReactorConfig(IOReactorConfig.DEFAULT)
                                .setHttp1Config(Http1Config.DEFAULT)
                                .create();
                httpAsyncRequester.start();*/
//                final AsyncPushConsumerRegistry pushConsumerRegistry = new AsyncPushConsumerRegistry();
/*                final IOEventHandlerFactory ioEventHandlerFactory = new HttpAsyncClientEventHandlerFactory(
                        new DefaultHttpProcessor(new H2RequestContent(), new H2RequestTargetHost(), new H2RequestConnControl()),
                        new HandlerFactory<AsyncPushConsumer>() {

                            @Override
                            public AsyncPushConsumer create(final HttpRequest request, final HttpContext context) throws HttpException {
//                                return pushConsumerRegistry.get(request);
                                return null;
                            }

                        },
                        HttpVersionPolicy.NEGOTIATE,
                        H2Config.DEFAULT,
                        Http1Config.DEFAULT,
                        CharCodingConfig.DEFAULT,
                        DefaultConnectionReuseStrategy.INSTANCE);
                final DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(
                        ioEventHandlerFactory,
                        IOReactorConfig.DEFAULT,
                        new DefaultThreadFactory("httpclient-dispatch", true),
                        LoggingIOSessionDecorator.INSTANCE,
                        LoggingExceptionCallback.INSTANCE,
                        null,
                        new Callback<IOSession>() {

                            @Override
                            public void execute(final IOSession ioSession) {
                                ioSession.enqueue(new ShutdownCommand(CloseMode.GRACEFUL), Command.Priority.IMMEDIATE);
                            }

                        });*/

                Future<AsyncConnectionEndpoint> connectFuture=service.myAsyncConnectionClient.asyncConnMgr
                        .connect(asyncConnectionEndpoint,
                                 defaultConnectingIOReactor,
//                                 httpAsyncRequester,
//                                 ioReactor,
                                 Timeout.ofSeconds(1),
                                HttpVersionPolicy.FORCE_HTTP_1,
                                clientContext,
                                new FutureCallback<AsyncConnectionEndpoint>() {
                                    @Override
                                    public void completed(AsyncConnectionEndpoint result) {
                                        Log.i(TAG," Connection Success");
                                     }

                                    @Override
                                    public void failed(Exception ex) {
                                        Log.e(TAG," Connection Failed "+ex.getMessage());
                                    }

                                    @Override
                                    public void cancelled() {
                                        Log.e(TAG," Connection Cancelled");
                                    }
                                }
                );
                try {
                    connectFuture.get();
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG,"Connection future get "+e.getMessage());
                    return;
                }
                if(asyncConnectionEndpoint==null)
                {
                    Log.e(TAG,"Connection future get is null");
                    return;
                }

                /*                final HttpHost host = new HttpHost("https", service.hostname, service.port);
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
                }*/
                break;
            case CMD_DISCONNECT:
 /*               try {
                    service.myAsyncConnectionClient.client.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }*/
        }
    }
}
