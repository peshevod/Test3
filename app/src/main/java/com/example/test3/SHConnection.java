package com.example.test3;

import android.util.Log;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SHConnection implements Runnable
{
    public final static int CMD_DISCONNECT=0;
    public final static int CMD_CONNECT=1;
    public final static int CMD_LOGIN=2;
    String TAG="TLS13 SHConnection";
    SHConnectionService service;

    public SHConnection(SHConnectionService service)
    {
        this.service=service;
    }

    @Override
    public void run() {
        Log.i(TAG,"Thread id="+Thread.currentThread().getId());
        
        switch (service.cmd) {
            case CMD_CONNECT:
                HttpHost httpHost=new HttpHost("https",service.hostname,service.port);
                HttpRoute httpRoute=new HttpRoute(httpHost);
                service.id++;
                service.leaseRequest=service.shConnectionClient.connMgr.lease(String.valueOf(service.id), httpRoute,null);
                if(service.leaseRequest!=null) {
                    try {
                        service.connectionEndpoint = service.leaseRequest.get(Timeout.ofSeconds(1));
                        service.shConnectionClient.connMgr.connect(service.connectionEndpoint, TimeValue.ofSeconds(5), service.basicHttpContext);
//                        service.shConnectionClient.connMgr.upgrade(service.connectionEndpoint, new BasicHttpContext());
                        service.main.connected=true;
                    } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
                        Log.e(TAG, e.getMessage());
                        service.main.connected = false;
                    }
                }
                else service.main.connected = false;
                break;
            case CMD_DISCONNECT:
                service.shConnectionClient.connMgr.release(service.connectionEndpoint, null, TimeValue.ofSeconds(5));
                service.main.connected = false;
                break;
        }
        if(service.main.connected)
        {
            service.main.login_state=MainActivity.BASIC_LOGIN_REQUIRED;
            Log.i(TAG,"Connected!");
//        serviceconnMgr.release(main.conn,null,TimeValue.ofHours(1));
        }
        else Log.e(TAG, "Not connected");
        service.main.homeViewModel.postConnected(service.main.connected);
    }
}
