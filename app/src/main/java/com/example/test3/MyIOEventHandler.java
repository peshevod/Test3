package com.example.test3;

import android.util.Log;

import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MyIOEventHandler implements IOEventHandler {

    private final String TAG="TLS13 EventHandler";
    @Override
    public void connected(IOSession session) throws IOException {
        Log.i(TAG,"Connected to "+session.getRemoteAddress().toString());
    }

    @Override
    public void inputReady(IOSession session, ByteBuffer src) throws IOException {
        Log.i(TAG,"Input ready "+session.getRemoteAddress().toString());
    }

    @Override
    public void outputReady(IOSession session) throws IOException {
        Log.i(TAG,"Output ready "+session.getRemoteAddress().toString());
    }

    @Override
    public void timeout(IOSession session, Timeout timeout) throws IOException {
        Log.e(TAG,"Timeout "+session.getRemoteAddress().toString());
    }

    @Override
    public void exception(IOSession session, Exception cause) {
        Log.e(TAG,"Exception "+cause.getMessage());
    }

    @Override
    public void disconnected(IOSession session) {
        Log.i(TAG,"Disconnected from "+session.getRemoteAddress().toString());
    }
}
