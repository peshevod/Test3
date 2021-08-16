package com.example.test3;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
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

public class TLS13 extends AsyncTask {

    private String server_string;
    private int server_port = 443;
    private static final String TLSV1_3 = "TLSv1.3";
    private static final String TLS_AES_128_GCM_SHA256 = "TLS_AES_128_GCM_SHA256";
    private SSLSocket sslSocket;
    private MainActivity main;
    private HttpsURLConnection conn;
    private String s;

    public TLS13(MainActivity main, String server_string, int server_port)
    {
        this.server_string=server_string;
        this.server_port=server_port;
        this.main=main;
        s="https://"+server_string+":"+server_port+"/";
    }

    public void getSslSocket() throws IOException {

        final String TAG = "TLS";
        try {
/*            KeyManagerFactory kmf;
            KeyStore ks;
            char[] passphrase = "passphrase".toCharArray();*/

           main.ctx = SSLContext.getInstance("TLSv1.3");
/*           kmf = KeyManagerFactory.getInstance("SunX509");
            ks = KeyStore.getInstance("JKS");

            ks.load(new FileInputStream("testkeys"), passphrase);

            kmf.init(ks, passphrase);
            main.ctx.init(kmf.getKeyManagers(), null, null);*/
            main.ctx.init(null, null, null);
            main.factory = new MySSLSocketFactory(main.ctx);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        Log.i(TAG, "socket factory created");

/*       Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            Log.i("TLS","provider: "+provider.getName());
            Set<Provider.Service> services = provider.getServices();
            for (Provider.Service service : services) {
                Log.i("TLS","  algorithm: "+service.getType()+" "+service.getAlgorithm());
            }
            Set<Object> props=provider.keySet();
            for(Object key : props)
            {
                Log.i("TLS","key "+key.toString()+" = "+provider.getProperty(key.toString()));
            }
        }*/


/*        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        sslSocket = (SSLSocket) sslSocketFactory.createSocket(server_string, server_port);

        //Setting the TSLv1.3 protocol
        sslSocket.setEnabledProtocols(new String[]{TLSV1_3});

        //Setting the Cipher: TLS_AES_128_GCM_SHA256
        sslSocket.setEnabledCipherSuites(new String[]{TLS_AES_128_GCM_SHA256});

        //Handshake
        sslSocket.startHandshake();*/
        URL url=new URL(s);
        conn= (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(main.factory);
    }

    public void sendRequest() throws IOException {

        /*        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(sslSocket.getOutputStream());*/
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Host","mm304.asuscomm.com");
        conn.setRequestProperty("Accept", "text/html");
//        conn.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(outputStreamWriter));
        printWriter.println("body");
       //        printWriter.println("GET / HTTP/1.1");
//        printWriter.println("\r\nHost: mm304.asuscomm.com\r\nAccept: text/html\r\n");
        printWriter.flush();
        printWriter.close();
        outputStreamWriter.close();
        Log.i("TLS","sent");
    }
    
    public void readResponse() throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        conn.getInputStream()));
/*                        sslSocket.getInputStream()),4096);*/
            String responseData;
            while ((responseData = in.readLine()) != null)
            Log.i("TLS",responseData);
            in.close();
    }

    public void test() throws IOException
    {
        getSslSocket();
        sendRequest();
        readResponse();
    }

    @Override
    protected Object doInBackground(Object... arg0){
        try {
            test();
        } catch (IOException e)
        {
            Log.e("TLS", e.getMessage());
        }
        return null;
    }
}
