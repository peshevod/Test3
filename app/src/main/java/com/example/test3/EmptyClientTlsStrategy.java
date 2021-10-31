package com.example.test3;

import android.util.Log;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.reactor.ssl.TransportSecurityLayer;
import org.apache.hc.core5.util.Timeout;

import java.net.SocketAddress;

public class EmptyClientTlsStrategy implements TlsStrategy {
    @Override
    public boolean upgrade(TransportSecurityLayer sessionLayer, HttpHost host, SocketAddress localAddress, SocketAddress remoteAddress, Object attachment, Timeout handshakeTimeout) {
        Log.i("TLS13", " Empty TLS Strategy "+ remoteAddress.toString()+" "+sessionLayer.toString());
        return true;
    }
}
