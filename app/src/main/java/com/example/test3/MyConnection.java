package com.example.test3;

import android.util.Log;


import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
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

    }

    public void connect()
    {
        HttpHost host=new HttpHost( "https",hostname,port);
        HttpRoute route=new HttpRoute(host);
        HttpClientContext context=new HttpClientContext();
        try {
            LeaseRequest leaseRequest=main.connMgr.lease("1",route, Timeout.ofMilliseconds(3000),null);
            main.conn = leaseRequest.get(Timeout.ofSeconds(10));  //block until connection
            if (!main.conn.isConnected()) {
                main.connMgr.connect(main.conn, Timeout.ofMilliseconds(3000), context);
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
        main.homeViewModel.postConnected(main.connected);
        Log.i("TLS13","Connected!");
//        callback.onComplete(new Result.Success<Boolean>(true));
        return;
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

    @Override
    public void run() {
        if(cmd==CMD_CONNECT) connect();
        if(cmd==CMD_DISCONNECT) disconnect();
        if(cmd==CMD_LOGIN) login();
    }
}


