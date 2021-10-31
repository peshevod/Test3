package com.example.test3;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.core5.http.protocol.BasicHttpContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SHConnectionService extends Service {

    ExecutorService pool;
    LeaseRequest leaseRequest;
    BasicHttpContext basicHttpContext=new BasicHttpContext();
    ConnectionEndpoint connectionEndpoint;
    SHConnection shConnection;
    SHConnectionClient shConnectionClient;
    public MainActivity main;
    String hostname;
    int port;
    int cmd;
    private final IBinder binder = new SHConnectionService.SHConnectionIBinder();
    int id;


    public void connect(String hostname, int port) {
        this.hostname=hostname;
        this.port=port;
//        this.hostname="www.blacktyres.ru";
//        this.port=443;
        shConnection=new SHConnection(this);
        cmd=SHConnection.CMD_CONNECT;
        pool.execute(shConnection);
    }

    public void disconnect()
    {
        cmd=SHConnection.CMD_DISCONNECT;
        shConnection=new SHConnection(this);
        pool.execute(shConnection);
    }

    public SHConnectionService() {
        pool = Executors.newCachedThreadPool();
    }

    public class SHConnectionIBinder extends Binder {
        SHConnectionService getService() {
            return SHConnectionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        id=1;
        shConnectionClient=new SHConnectionClient(this);
        return binder;
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}