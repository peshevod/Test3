package com.example.test3;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.BasicHttpContext;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

public class MyAsyncConnectionService extends Service {

    MyAsyncConnection myAsyncConnection;
    MyAsyncConnetcionClient myAsyncConnectionClient;
    public MainActivity main;
    String hostname;
    int port;
    int cmd;
    private final IBinder binder = new MyAsyncConnectionIBinder();
    ExecutorService pool;

    public MyAsyncConnectionService() {
/*        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }*/
//        basicHttpContext=new BasicHttpContext();
        pool = Executors.newCachedThreadPool();
    }

    public void asyncConnect(String hostname, int port) {
        this.hostname=hostname;
        this.port=port;
        cmd=MyAsyncConnection.CMD_CONNECT;
        pool.execute(myAsyncConnection);
    }

    public void asyncDisconnect()
    {
        cmd=MyAsyncConnection.CMD_DISCONNECT;
        pool.execute(myAsyncConnection);
    }


    public class MyAsyncConnectionIBinder extends Binder {
        MyAsyncConnectionService getService() {
            return MyAsyncConnectionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        myAsyncConnectionClient=new MyAsyncConnetcionClient(this);
        pool.execute(myAsyncConnectionClient);
        myAsyncConnection=new MyAsyncConnection(this);
        return binder;
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}