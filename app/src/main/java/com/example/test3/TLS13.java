package com.example.test3;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLS13 extends AsyncTask {

    private String server_string;
    private int server_port = 443;
    private static final String TLSV1_3 = "TLSv1.3";
    private static final String TLS_AES_128_GCM_SHA256 = "TLS_AES_128_GCM_SHA256";
    private SSLSocket sslSocket;

    public TLS13(String server_string, int server_port)
    {
        this.server_string=server_string;
        this.server_port=server_port;
    }

    public void getSslSocket() throws IOException {

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
        printWriter.println("GET /test-url HTTP/1.1\r\n");
        printWriter.flush();
    }
    
    public void readResponse() throws IOException {

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        sslSocket.getInputStream()));
            String responseData;
            while ((responseData = in.readLine()) != null)
                System.out.println(responseData);

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
            e.printStackTrace();
        }
        return null;
    }
}
