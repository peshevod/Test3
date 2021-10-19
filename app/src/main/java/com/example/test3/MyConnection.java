package com.example.test3;

import static android.icu.util.TimeUnit.*;

import android.icu.util.TimeUnit;
import android.util.Log;


import com.example.test3.data.Result;
import com.example.test3.data.model.LoggedInUser;
import com.example.test3.ui.login.LoggedInUserView;
import com.example.test3.ui.login.LoginResult;

import android.util.Base64;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.impl.bootstrap.AsyncRequesterBootstrap;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class MyConnection implements Runnable {

    final String TAG="MyConnection";
//    final String[] protocols={"TLSv1.3"};
//    final String[] ciphers={"TLS_AES_128_GCM_SHA256"};
//    private SSLConnectionSocketFactory scsf;
//    private final HttpClientContext ctx=HttpClientContext.create();
    String string1,string2,hostname,username,password;
    public static final int CMD_DISCONNECT=0;
    public static final int CMD_CONNECT=1;
    public static final int CMD_LOGIN=2;
    int cmd;
//    MyConnectionCallback<Boolean> callback;
    int port;
    MainActivity main;

    public MyConnection(MainActivity main, String hostname, int port) {
        this.main = main;
        this.hostname = hostname;
        this.port = port;
        this.cmd = CMD_CONNECT;
    }

    public MyConnection(MainActivity main)
    {
        this.main = main;
        this.cmd = CMD_DISCONNECT;
    }

    public MyConnection(MainActivity main, String username, String password)
    {
        this.main = main;
        this.username=username;
        this.password=password;
        this.cmd = CMD_LOGIN;
    }

    public void login()
    {
        BasicCredentialsProvider provider=new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(main.host,"Monitor",main.host.getSchemeName()),new UsernamePasswordCredentials(username,password.toCharArray()));
        CloseableHttpClient httpClient=HttpClientBuilder.create()
                .setConnectionManager(main.connMgr)
                .setDefaultCredentialsProvider(provider)
                .setRequestExecutor(main.httpRequestExecutor)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofSeconds(3))
                        .setConnectTimeout(Timeout.ofSeconds(3))
                        .build())
                .build();
        try {
            BasicClassicHttpRequest request1=new BasicClassicHttpRequest(Method.GET, main.host,"/monitor");
            CloseableHttpResponse response1= httpClient.execute(main.host,request1, main.basicHttpContext);
            Log.i("TLS13","end request1");
            int code=response1.getCode();
            Log.i("TLS13",response1.getReasonPhrase()+" "+code);

            main.connMgr.release(main.conn,null,TimeValue.ofHours(1));
//            response1.close();
            if(code==401)
            {
//                response.close();
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8),Base64.DEFAULT);
//                        auth.getBytes(StandardCharsets.ISO_8859_1));
                String authHeader = "Basic " + new String(encodedAuth);
                request1.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                Log.i("TLS13","start request1 one more"+request1.toString());
                response1=httpClient.execute(main.host,request1, main.basicHttpContext);
                code=response1.getCode();
                Log.i("TLS13",response1.getReasonPhrase()+" "+code);
                if(code==200)
                {
                    byte[] bytes=new byte[256];
                    long l=response1.getEntity().getContentLength();
                    InputStream is=response1.getEntity().getContent();
                    int m=0;
                    int n=256;
                    int k=0;
                    while(((k=is.read(bytes,m,n))!=-1) && n>0 && m<l)
                    {
                        m+=k;
                        n-=k;
                    }
                    String welcome=new String(bytes, 0,m,StandardCharsets.UTF_8);
                    LoggedInUser loggedInUser=new LoggedInUser(username,password,main.host.getHostName(),"007",welcome);
                    main.loginViewModel.getLoginRepository().setLoggedInUser(loggedInUser);
                    main.login_state=MainActivity.LOGGED_IN;
                    main.loginViewModel.getLoginResult().postValue(new LoginResult(loggedInUser));
                    Log.i("TLS13","user 007 "+welcome);
                }
            }
        } catch (IOException e) {
            main.loginViewModel.getLoginResult().postValue(new LoginResult(new Integer(e.hashCode())));
            Log.i("TLS13",e.getMessage());
        }
    }

    public void connect()
    {
        main.host=new HttpHost( "https",hostname,port);
        HttpRoute route=new HttpRoute(main.host);
         try {
            long t0=System.currentTimeMillis();
            LeaseRequest leaseRequest=main.connMgr.lease("1",route, Timeout.ofMilliseconds(3000),null);
            main.conn = leaseRequest.get(Timeout.ofSeconds(10));  //block until connection
            if (!main.conn.isConnected()) {
                Log.i(TAG, "No connection yet, connecting...");
                main.connMgr.connect(main.conn, Timeout.ofMilliseconds(5000), main.basicHttpContext);
//                main.connMgr.upgrade(main.conn, main.basicHttpContext);
             } else Log.i(TAG, "Connection is already opened");
        } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
            Log.e(TAG,e.getMessage());
            main.connected=false;
            main.homeViewModel.postConnected(main.connected);
//            callback.onComplete(new Result.Error(e));
            return;
        }
        main.connected=true;
        Log.i("TLS13","Connected!");
        main.connMgr.release(main.conn,null,TimeValue.ofHours(1));
        main.login_state=MainActivity.BASIC_LOGIN_REQUIRED;
        main.homeViewModel.postConnected(main.connected);
//        callback.onComplete(new Result.Success<Boolean>(true));
        return;
    }




    public void asyncConnect() {
        main.host = new HttpHost("https", hostname, port);
        HttpRoute route = new HttpRoute(main.host);

        FutureCallback<AsyncConnectionEndpoint> fcb_connect = new FutureCallback<AsyncConnectionEndpoint>() {

            @Override
            public void completed(AsyncConnectionEndpoint result) {
                main.connected = true;
                main.homeViewModel.postConnected(main.connected);
                Log.i("TLS13", "Connected!");
                main.asyncConnMgr.release(result, null, TimeValue.ofHours(1));
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
                main.asyncConn = result;
                if (!main.asyncConn.isConnected()) {
                    Log.i(TAG, "No connection yet, connecting...");
                    main.asyncConnMgr.connect(main.asyncConn, AsyncRequesterBootstrap.bootstrap().create(), Timeout.ofMilliseconds(5000),
                            null, main.basicHttpContext, fcb_connect);
                }
                else Log.i(TAG, "Connection is already opened");
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

        long t0 = System.currentTimeMillis();
        main.asyncConnMgr.lease("1", route, null, Timeout.ofMilliseconds(3000), fcb_lease);
     }

    public void disconnect()
    {
        main.connMgr.release(main.conn, null, TimeValue.ofMilliseconds(10));
        try {
            main.conn.close();
        } catch (IOException e) {
        }
        main.connected=false;
        main.homeViewModel.postConnected(main.connected);
    }

    public void asyncDisconnect()
    {
        main.asyncConnMgr.release(main.asyncConn, null, TimeValue.ofMilliseconds(10));
        try {
            main.asyncConn.close();
        } catch (IOException e) {
        }
        main.connected=false;
        main.homeViewModel.postConnected(main.connected);
    }

    @Override
    public void run() {
        if(cmd==CMD_CONNECT) connect();
        if(cmd==CMD_DISCONNECT) disconnect();
        if(cmd==CMD_LOGIN) login();
    }
}


