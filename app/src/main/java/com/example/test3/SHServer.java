package com.example.test3;

import android.util.Log;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.http.HttpHost;

public class SHServer {
    public String name;
    public String hostname;
    public int port;
    public HttpHost httpHost;
    public HttpRoute httpRoute;
    ConnectionEndpoint connectionEndpoint;

    public SHServer(String name, String hostname, int port)
    {
        this.name=name;
        this.hostname=hostname;
        this.port=port;
        httpHost= new HttpHost("https",hostname,port);
        httpRoute= new HttpRoute(RoutingSupport.normalize(httpHost, DefaultSchemePortResolver.INSTANCE),null,true);
        connectionEndpoint=null;
    }

    public boolean isConnected()
    {
        return (connectionEndpoint!=null && connectionEndpoint.isConnected());
    }
}
