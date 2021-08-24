package com.example.test3;

import android.os.AsyncTask;
import android.util.Log;


import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MySSLSocketFactory extends SSLConnectionSocketFactory {

    private SSLSocketFactory factory;
    private SSLContext ctx;
    String cipher[]={"TLS_AES_128_GCM_SHA256"};

    public MySSLSocketFactory(SSLContext ctx)
    {
        super(ctx);
        this.ctx=ctx;
//        factory=ctx.getSocketFactory();
    }

    public String[] getDefaultCipherSuites() {
        return cipher;
    }

    public String[] getSupportedCipherSuites() {
        return cipher;
    }

/*    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return factory.createSocket(s,host,port,autoClose);
    }


    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return factory.createSocket(host,port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return factory.createSocket(host,port,localHost,localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return factory.createSocket(host,port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return factory.createSocket(address,port,localAddress,localPort);
    }

    @Override
    public Socket createLayeredSocket(Socket socket, String target, int port, HttpContext context) throws IOException, UnknownHostException {
        return null;
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        return null;
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
        return null;
    }*/
}

