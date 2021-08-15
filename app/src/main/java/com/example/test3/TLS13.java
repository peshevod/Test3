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
import java.net.URL;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

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

    public TLS13(MainActivity main, String server_string, int server_port)
    {
        this.server_string=server_string;
        this.server_port=server_port;
        this.main=main;
    }

    public void getSslSocket() throws IOException {

        final String TAG = "TLS";
/*        try {
            KeyManagerFactory kmf;
            KeyStore ks;
            char[] passphrase = "passphrase".toCharArray();

            main.ctx = SSLContext.getInstance("TLSv1.3");
            kmf = KeyManagerFactory.getInstance("SunX509");
            ks = KeyStore.getInstance("JKS");

            ks.load(new FileInputStream("testkeys"), passphrase);

            kmf.init(ks, passphrase);
            main.ctx.init(kmf.getKeyManagers(), null, null);

            main.factory = main.ctx.getSocketFactory();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        Log.e(TAG, "socket factory created");*/

        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        sslSocket = (SSLSocket) sslSocketFactory.createSocket(server_string, server_port);

        //Setting the TSLv1.3 protocol
        sslSocket.setEnabledProtocols(new String[]{TLSV1_3});

        //Setting the Cipher: TLS_AES_128_GCM_SHA256
        sslSocket.setEnabledCipherSuites(new String[]{TLS_AES_128_GCM_SHA256});

        //Handshake
        sslSocket.startHandshake();

    }

    public void sendRequest() throws IOException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(sslSocket.getOutputStream());
        PrintWriter printWriter = new PrintWriter(new BufferedWriter(outputStreamWriter));
        printWriter.println("GET / HTTP/1.1\r\nHost: mm304.asuscomm.com\r\nAccept: text/html\r\n");
        printWriter.flush();
    }
    
    public void readResponse() throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        sslSocket.getInputStream()),4096);
            String responseData;
            while ((responseData = in.readLine()) != null)
            Log.i("TLS",responseData);
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
