package com.example.test3;

import android.os.AsyncTask;
import android.telecom.ConnectionRequest;
import android.util.Log;


import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        HttpHost host=new HttpHost( "https",hostname,port);
        HttpRoute route=new HttpRoute(host);
        try {
            LeaseRequest leaseRequest=main.connMgr.lease("1",route, Timeout.ofMilliseconds(3000),null);
            main.conn = leaseRequest.get(Timeout.ofSeconds(10));
            if (!main.conn.isConnected()) {
                main.connMgr.connect(main.conn, Timeout.ofMilliseconds(3000), ctx);
             } else Log.i(TAG, "Connection is already opened");
        } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
            Log.e(TAG,e.getMessage());
            main.connected=false;
            return false;
        }
        main.connected=true;
        return true;
    }

    public void disconnect()
    {
        main.connMgr.release(main.conn, null, TimeValue.ofMinutes(1));
        main.connected=false;
    }

    @Override
    protected Void doInBackground(Boolean... b) {
        if(cmd==CMD_CONNECT) connect();
        if(cmd==CMD_DISCONNECT) disconnect();
        return null;
    }
}
