package com.example.test3;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MyConnection extends AsyncTask<Boolean, Void, Void> {

    final String TAG="MyConnection";
    final String[] protocols={"TLSv1.3"};
    final String[] ciphers={"TLS_AES_128_GCM_SHA256"};
    private SSLConnectionSocketFactory scsf;
    private final HttpClientContext ctx=HttpClientContext.create();
    String hostname;
    public static final int CMD_DISCONNECT=0;
    public static final int CMD_CONNECT=1;
    int cmd;

    int port;
    MainActivity main;

    public MyConnection(int cmd, MainActivity main,String hostname,int port)
    {
        this.main=main;
        this.hostname=hostname;
        this.port=port;
        this.cmd=cmd;
/*        try {
            scsf=new SSLConnectionSocketFactory(SSLContext.getDefault(), protocols, ciphers, new org.apache.http.conn.ssl.DefaultHostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return false;
                }
            });
         } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "No algorithm");
        }
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", scsf)
                .build();
        connMgr= new BasicHttpClientConnectionManager(registry);*/
    }

    @Override
    public void onPostExecute(Void v)
    {
        main.homeViewModel.setConnected(main.connected);
    }

    public boolean connect()
    {
        HttpHost host=new HttpHost(hostname,port, "https");
        HttpRoute route=new HttpRoute(host);
        ConnectionRequest connRequest=main.connMgr.requestConnection(route,null);
        try {
            main.conn = connRequest.get(10, TimeUnit.SECONDS);
            if (!main.conn.isOpen()) {
                // establish connection based on its route info
                try {
                    main.connMgr.connect(main.conn, route, 1000, ctx);
                    main.connMgr.routeComplete(main.conn, route, ctx);
                } catch (IOException e) {
                    Log.e(TAG,e.getMessage());
                    main.connected=false;
                    return false;
                }
             } else Log.i(TAG, "Connection is not open");
        } catch (InterruptedException | ExecutionException | ConnectionPoolTimeoutException e) {
            Log.e(TAG,e.getMessage());
            main.connected=false;
            return false;
        }
        main.connected=true;
        return true;
    }

    public void disconnect()
    {
        main.connMgr.releaseConnection(main.conn, null, 1, TimeUnit.MINUTES);
        main.connected=false;
    }

    @Override
    protected Void doInBackground(Boolean... b) {
        if(cmd==CMD_CONNECT) connect();
        if(cmd==CMD_DISCONNECT) disconnect();
        return null;
    }
}
