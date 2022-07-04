package com.example.test3;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.example.test3.data.Result;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.BasicHttpContext;

import java.util.List;
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
    String username, password;
    int port;
    private final IBinder binder = new SHConnectionService.SHConnectionIBinder();
    int id;
    HttpHost httpHost;
    HttpRoute httpRoute;
    String token;
    List<MyDevice> devices;
    public List<MySession> sessions;
    private final String TAG="TLS13 SHConnectionService";

    public MutableLiveData<Boolean> requestCompleted = new MutableLiveData<>();
    public MutableLiveData<Result> result = new MutableLiveData<com.example.test3.data.Result>();

    public String getHostname()
    {
        return hostname;
    }

    public void connect(String hostname, int port)
    {
        this.hostname=hostname;
        this.port=port;
        SHConnection shConnection=new SHConnection(this,SHConnection.CMD_CONNECT);
        pool.execute(shConnection);
    }

    public void disconnect()
    {
        SHConnection shConnection=new SHConnection(this,SHConnection.CMD_DISCONNECT);
        pool.execute(shConnection);
    }
    public void login(String username, String password) {
        this.username=username;
        this.password=password;
        SHConnection shConnection=new SHConnection(this, SHConnection.CMD_LOGIN);
        pool.execute(shConnection);
    }

    public List<MyDevice> getDevices() {
        SHConnection shConnection=new SHConnection(this,SHConnection.CMD_DEVICES);
        requestCompleted.postValue(false);
        pool.execute(shConnection);
        return devices;
    }

    public List<MySession> getSessions() {
        SHConnection shConnection=new SHConnection(this,SHConnection.CMD_SESSIONS);
        requestCompleted.setValue(false);
        pool.execute(shConnection);
        return sessions;
    }


    public SHConnectionService() {
        pool = Executors.newCachedThreadPool();
        id=1;
        shConnectionClient=new SHConnectionClient(this);
    }

    public class SHConnectionIBinder extends Binder {
        SHConnectionService getService() {
            return SHConnectionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        return binder;
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}