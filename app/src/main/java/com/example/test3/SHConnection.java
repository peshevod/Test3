package com.example.test3;

import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;

import com.example.test3.data.Result;
import com.example.test3.data.model.LoggedInUser;
import com.example.test3.ui.login.LoginResult;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SHConnection implements Runnable
{
    public final static int CMD_DISCONNECT=0;
    public final static int CMD_CONNECT=1;
    public final static int CMD_LOGIN=2;
    public final static int CMD_DEVICES=3;
    public final static int CMD_SESSIONS=4;
    int cmd;
    String TAG="TLS13 SHConnection";
    SHConnectionService service;

    public SHConnection(SHConnectionService service, int cmd)
    {
        this.service=service;
        this.cmd=cmd;
    }

/*    public void onLoginSuccess(String welcome){}

    public void onLoginFailure(Exception error){}

    public void onLoginStart(){}*/

    public void login()
    {
        BasicClassicHttpRequest request1=new BasicClassicHttpRequest(Method.GET, service.httpHost,"/login");
        String auth = service.username + ":" + service.password;
        byte[] encodedAuth = new byte[0];
        try {
            encodedAuth = Base64.encode(auth.getBytes("windows-1251"),Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
//                        auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        request1.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        try(CloseableHttpResponse response1= service.shConnectionClient.httpClient.execute(service.httpHost,request1, service.basicHttpContext))
        {
            int code = response1.getCode();
            Log.i(TAG, response1.getReasonPhrase() + " " + code);
            if (code == 200) {
                String welcome = "";
                try(JsonReader reader = new JsonReader(new InputStreamReader(response1.getEntity().getContent(), "windows-1251"))) {
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
//                onLoginSuccess(welcome);
                service.result.postValue(new Result.Success<LoggedInUser>(new LoggedInUser(service.getHostname(), service.username, service.password, welcome)));
                Log.i(TAG, service.username + " " + welcome);
            }
            else
            {
                service.result.postValue(new Result.Error(new Exception(""+code+"("+response1.getReasonPhrase()+")")));
                Log.i(TAG, "Login Failed "+response1.getReasonPhrase() + " " + code);
            }
        }
        catch (IOException e) {
//            onLoginFailure(e);
            service.result.postValue(new Result.Error(e));
            Log.i(TAG,e.getMessage());
        }
        postToken(true);
    }

    public void postToken(boolean add)
    {
        BasicClassicHttpRequest request1=new BasicClassicHttpRequest(Method.POST, service.httpHost,"/monitor/token");
        request1.addHeader(new BasicHeader("Authorization", "Bearer "+service.token));
        request1.setEntity(new StringEntity((add ? "+" : "-" )+service.main.token));
        try(CloseableHttpResponse response1= service.shConnectionClient.httpClient.execute(service.httpHost,request1, service.basicHttpContext))
        {
            int code = response1.getCode();
            Log.i(TAG, response1.getReasonPhrase() + " " + code);
            if (code == 200) Log.i(TAG,"Post Token Success");
            else Log.e(TAG,"Post token Failure "+code+response1.getReasonPhrase());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
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
                String devName = null, devEui = null, version = null, Sensor1=null, Sensor2=null;
                try(JsonReader reader = new JsonReader(new InputStreamReader(response1.getEntity().getContent(), "windows-1251")))
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
                                    else if (name.equalsIgnoreCase("Sensor1"))
                                        Sensor1 = reader.nextString();
                                    else if (name.equalsIgnoreCase("Sensor2"))
                                        Sensor2 = reader.nextString();
                                    else reader.skipValue();
                                }
                                service.devices.add(new MyDevice(devName, devEui, version, Sensor1, Sensor2));
                                Log.i(TAG, "add device " + devName + " Eui=" + devEui + " version=" + version+ " Sensor1=" + Sensor1+ " Sensor2=" + Sensor2);
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

    public void getSessions()
    {
        service.sessions = new ArrayList<MySession>();
        BasicClassicHttpRequest request1=new BasicClassicHttpRequest(Method.GET, service.httpHost,"/monitor/sessions");
        request1.addHeader(new BasicHeader("Authorization", "Bearer "+service.token));
        try(CloseableHttpResponse response1= service.shConnectionClient.httpClient.execute(service.httpHost,request1, service.basicHttpContext))
        {
            int code = response1.getCode();
            Log.i(TAG, response1.getReasonPhrase() + " " + code);
            if (code == 200)
            {
                MySession session=null;
                Log.i(TAG, "Response=\n"+response1.getEntity().toString());
                try(JsonReader reader = new JsonReader(new InputStreamReader(response1.getEntity().getContent(), "windows-1251")))
                {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name0 = reader.nextName();
                        Log.i(TAG,"Name="+name0);
                        if(name0.equalsIgnoreCase("Sessions")) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String name = reader.nextName();
                                    if (name.equalsIgnoreCase("Device"))
                                        session=new MySession(reader.nextString(), service.main);
                                    else if (name.equalsIgnoreCase("time"))
                                        session.setTime(reader.nextLong());
                                    else if (name.equalsIgnoreCase("devnonce"))
                                        session.devNonce = reader.nextInt();
                                    else if (name.equalsIgnoreCase("fcntup"))
                                        session.fcntUp = reader.nextLong();
                                    else if (name.equalsIgnoreCase("temperature"))
                                        session.temperature = reader.nextInt();
                                    else if (name.equalsIgnoreCase("batlevel"))
                                        session.battery = reader.nextInt();
                                    else if (name.equalsIgnoreCase("rssi"))
                                        session.remoteRSSI = reader.nextInt();
                                    else if (name.equalsIgnoreCase("snr"))
                                        session.remoteSNR = reader.nextInt();
                                    else if (name.equalsIgnoreCase("local_rssi"))
                                        session.localRssi = reader.nextInt();
                                    else if (name.equalsIgnoreCase("local_snr"))
                                        session.localSnr = reader.nextInt();
                                    else if (name.equalsIgnoreCase("power"))
                                        session.remotePower = reader.nextInt();
                                    else if (name.equalsIgnoreCase("local_power"))
                                        session.localPower = reader.nextInt();
                                    else if (name.equalsIgnoreCase("values"))
                                        session.values = reader.nextInt();
                                    else if (name.equalsIgnoreCase("Sensor1"))
                                        session.sensor1 = reader.nextString();
                                    else if (name.equalsIgnoreCase("Sensor2"))
                                        session.sensor2 = reader.nextString();
                                    else reader.skipValue();
                                }
                                service.sessions.add(session);
                                Log.i(TAG, "add session " + session.devName + " time=" + session.lastDataTime + " devnonce=" + session.devNonce);
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
        
        switch (cmd) {
            case CMD_CONNECT:
                service.httpHost= new HttpHost("https",service.hostname,service.port);
                service.httpRoute= new HttpRoute(RoutingSupport.normalize(service.httpHost, DefaultSchemePortResolver.INSTANCE),null,true);
                if(service.connectionEndpoint!=null && service.connectionEndpoint.isConnected())
                {
                    Log.i(TAG, "Already connected");
                    service.main.connection_state.setValue(MainActivity.CONNECTED);
                    break;
                }
                service.id++;
                service.leaseRequest=service.shConnectionClient.connMgr.lease(String.valueOf(service.id), service.httpRoute,null);
                if(service.leaseRequest!=null) {
                    try {
                        service.connectionEndpoint = service.leaseRequest.get(Timeout.ofSeconds(1));
                        service.shConnectionClient.connMgr.connect(service.connectionEndpoint, TimeValue.ofSeconds(5), service.basicHttpContext);
                        service.main.connection_state.postValue(MainActivity.CONNECTED);
                    } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
                        Log.e(TAG, e.getMessage());
                        service.main.connection_state.postValue(MainActivity.NOT_CONNECTED);
                    }
                }
                else
                {
                    Log.i(TAG,"leaseRequest=null");
                    service.main.connection_state.postValue(MainActivity.NOT_CONNECTED);
                }
                service.shConnectionClient.connMgr.release(service.connectionEndpoint, null, TimeValue.ofSeconds(900));
                break;
            case CMD_DISCONNECT:
                service.connectionEndpoint.close(CloseMode.GRACEFUL);
                service.shConnectionClient.connMgr.release(service.connectionEndpoint, null, TimeValue.ofSeconds(0));
                service.main.connection_state.postValue(MainActivity.NOT_CONNECTED);
                break;
            case CMD_LOGIN:
                login();
                break;
            case CMD_SESSIONS:
                getSessions();
                break;
            case CMD_DEVICES:
                getDevices();
                break;
        }
        if(service.main.connection_state.getValue()==MainActivity.CONNECTED)
        {
            service.main.homeViewModel.postConnected(true);
            Log.i(TAG,"Connected!");
        }
        else if(service.main.connection_state.getValue()==MainActivity.NOT_CONNECTED)
        {
            Log.e(TAG, "Not connected");
            service.main.homeViewModel.postConnected(false);
        }
    }
}
