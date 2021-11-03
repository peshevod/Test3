package com.example.test3;

import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;

import com.example.test3.data.model.LoggedInUser;
import com.example.test3.ui.login.LoginResult;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SHConnection implements Runnable
{
    public final static int CMD_DISCONNECT=0;
    public final static int CMD_CONNECT=1;
    public final static int CMD_LOGIN=2;
    public final static int CMD_DEVICES=3;
    String TAG="TLS13 SHConnection";
    SHConnectionService service;

    public SHConnection(SHConnectionService service)
    {
        this.service=service;
    }

    public void login()
    {
        BasicClassicHttpRequest request1=new BasicClassicHttpRequest(Method.GET, service.httpHost,"/login");
        String auth = service.username + ":" + service.password;
        byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.UTF_8),Base64.DEFAULT);
//                        auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        request1.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        try(CloseableHttpResponse response1= service.shConnectionClient.httpClient.execute(service.httpHost,request1, service.basicHttpContext))
        {
            int code = response1.getCode();
            Log.i(TAG, response1.getReasonPhrase() + " " + code);
            if (code == 200) {
                String welcome = "";
                try(JsonReader reader = new JsonReader(new InputStreamReader(response1.getEntity().getContent(), "UTF-8"))) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equalsIgnoreCase("token")) service.token = reader.nextString();
                        else if (name.equalsIgnoreCase("welcome"))
                            welcome = reader.nextString();
                        else reader.skipValue();
                    }
                    reader.endObject();
                }
                LoggedInUser loggedInUser = new LoggedInUser(service.username, service.password, service.httpHost.getHostName(), service.username, welcome);
                service.main.loginViewModel.getLoginRepository().setLoggedInUser(loggedInUser);
                service.main.login_state = MainActivity.LOGGED_IN;
                service.main.loginViewModel.getLoginResult().postValue(new LoginResult(loggedInUser));
                Log.i(TAG, service.username + " " + welcome);
                service.shConnectionClient.connMgr.release(service.connectionEndpoint, null, TimeValue.ofSeconds(1));
            }
        }
        catch (IOException e) {
            service.main.loginViewModel.getLoginResult().postValue(new LoginResult(new Integer(e.hashCode())));
            Log.i(TAG,e.getMessage());
        }
//        service.requestCompleted.postValue(true);
    }

    public void getDevices()
    {
        service.devices = new ArrayList<MyDevice>();
        BasicClassicHttpRequest request1=new BasicClassicHttpRequest(Method.GET, service.httpHost,"/devices");
        request1.addHeader(new BasicHeader("Authorization", "Bearer "+service.token));
        try(CloseableHttpResponse response1= service.shConnectionClient.httpClient.execute(service.httpHost,request1, service.basicHttpContext))
        {
            int code = response1.getCode();
            Log.i(TAG, response1.getReasonPhrase() + " " + code);
            if (code == 200)
            {
                String devName = null, devEui = null, version = null;
                try(JsonReader reader = new JsonReader(new InputStreamReader(response1.getEntity().getContent(), "UTF-8")))
                {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name0 = reader.nextName();
                        Log.i(TAG,"Name="+name0);
                        if(name0.equalsIgnoreCase("Devices")) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String name = reader.nextName();
                                    if (name.equalsIgnoreCase("DevName"))
                                        devName = reader.nextString();
                                    else if (name.equalsIgnoreCase("DevEui"))
                                        devEui = reader.nextString();
                                    else if (name.equalsIgnoreCase("version"))
                                        version = reader.nextString();
                                    else reader.skipValue();
                                }
                                service.devices.add(new MyDevice(devName, devEui, version));
                                Log.i(TAG, "add device " + devName + " Eui=" + devEui + " version=" + version);
                                reader.endObject();
                            }
                            reader.endArray();
                        }
                        else reader.skipValue();
                    }
                    reader.endObject();
                }
            }
        } catch (IOException e)
        {
            Log.e(TAG, e.getMessage());
        }
        service.requestCompleted.postValue(true);
    }

    @Override
    public void run() {
        Log.i(TAG,"Thread id="+Thread.currentThread().getId());
        
        switch (service.cmd) {
            case CMD_CONNECT:
                service.httpHost=new HttpHost("https",service.hostname,service.port);
                service.httpRoute=new HttpRoute(service.httpHost);
                if(service.connectionEndpoint!=null && service.connectionEndpoint.isConnected())
                {
                    Log.i(TAG, "Already connected");
                    service.main.connected=true;
                    break;
                }
                service.id++;
                service.leaseRequest=service.shConnectionClient.connMgr.lease(String.valueOf(service.id), service.httpRoute,null);
                if(service.leaseRequest!=null) {
                    try {
                        service.connectionEndpoint = service.leaseRequest.get(Timeout.ofSeconds(1));
                        service.shConnectionClient.connMgr.connect(service.connectionEndpoint, TimeValue.ofSeconds(5), service.basicHttpContext);
                        service.main.connected=true;
                    } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
                        Log.e(TAG, e.getMessage());
                        service.main.connected = false;
                    }
                }
                else
                {
                    Log.i(TAG,"leaseRequest=null");
                    service.main.connected = false;
                }
                service.shConnectionClient.connMgr.release(service.connectionEndpoint, null, TimeValue.ofSeconds(1));
                break;
            case CMD_DISCONNECT:
                service.shConnectionClient.connMgr.release(service.connectionEndpoint, null, TimeValue.ofSeconds(5));
                service.main.connected = false;
                break;
            case CMD_LOGIN:
                login();
                break;
            case CMD_DEVICES:
                getDevices();
                break;
        }
        if(service.main.connected)
        {
            service.main.login_state=MainActivity.BASIC_LOGIN_REQUIRED;
            Log.i(TAG,"Connected!");
        }
        else Log.e(TAG, "Not connected");
        service.main.homeViewModel.postConnected(service.main.connected);
    }
}
