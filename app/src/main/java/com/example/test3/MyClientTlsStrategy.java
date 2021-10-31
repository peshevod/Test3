package com.example.test3;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.app.Service;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ssl.TlsCiphers;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.ssl.H2TlsSupport;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;
import org.apache.hc.core5.reactor.ssl.SSLSessionVerifier;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.reactor.ssl.TransportSecurityLayer;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

public class MyClientTlsStrategy implements TlsStrategy {

//    private TlsStrategy tlsStrategy;
//    SSLContext ctx=null;
    private SSLContext sslContext=null;
    private final String[] supportedProtocols={"TLSv1.3"};
    private final String[] supportedCipherSuites={"TLS_AES_128_GCM_SHA256"};
    private final SSLBufferMode sslBufferManagement=SSLBufferMode.STATIC;
    private final HostnameVerifier hostnameVerifier=new DefaultHostnameVerifier(null);
    private final TlsSessionValidator tlsSessionValidator=new TlsSessionValidator();
    final String TAG="TLS13 MyClientTlsStrategy";


    public MyClientTlsStrategy(MyAsyncConnectionService service)
    {
        super();
        try {
            ProviderInstaller.installIfNeeded(service.getApplicationContext());
            sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (KeyManagementException | NoSuchAlgorithmException | GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e("TLS13", e.getMessage());
        }
/*        tlsStrategy = ClientTlsStrategyBuilder.create()
                .setSslContext(ctx)
                .setTlsVersions("TLSv1.3")
                .setCiphers("TLS_AES_128_GCM_SHA256")
                .setHostnameVerifier(new DefaultHostnameVerifier(null))
                .build();*/
    }


/*    public boolean upgrade(TransportSecurityLayer sessionLayer, HttpHost host, SocketAddress localAddress, SocketAddress remoteAddress, Object attachment, Timeout handshakeTimeout) {
        Log.i("TLS13", "before TLssession="+sessionLayer.toString()+" HandshakeTimeout="+handshakeTimeout.toString());
        boolean x=tlsStrategy.upgrade(sessionLayer, host, localAddress, remoteAddress, attachment, handshakeTimeout);
        Log.i("TLS13", "after TLssession="+sessionLayer.toString()+" tlsStrategy upgrade="+x);
        return x;
    }*/

    @Override
    public boolean upgrade(
            final TransportSecurityLayer tlsSession,
            final HttpHost host,
            final SocketAddress localAddress,
            final SocketAddress remoteAddress,
            final Object attachment,
            final Timeout handshakeTimeout) {
        Log.i(TAG, "Start Tls");
        tlsSession.startTls(sslContext, host, sslBufferManagement, new SSLSessionInitializer() {

            @Override
            public void initialize(final NamedEndpoint endpoint, final SSLEngine sslEngine) {

                final HttpVersionPolicy versionPolicy = attachment instanceof HttpVersionPolicy ?
                        (HttpVersionPolicy) attachment : HttpVersionPolicy.NEGOTIATE;

                final SSLParameters sslParameters = sslEngine.getSSLParameters();
                if (supportedProtocols != null) {
                    sslParameters.setProtocols(supportedProtocols);
                } else if (versionPolicy != HttpVersionPolicy.FORCE_HTTP_1) {
                    sslParameters.setProtocols(TLS.excludeWeak(sslParameters.getProtocols()));
                }
                if (supportedCipherSuites != null) {
                    sslParameters.setCipherSuites(supportedCipherSuites);
                } else if (versionPolicy == HttpVersionPolicy.FORCE_HTTP_2) {
                    sslParameters.setCipherSuites(TlsCiphers.excludeH2Blacklisted(sslParameters.getCipherSuites()));
                }

                if (versionPolicy != HttpVersionPolicy.FORCE_HTTP_1) {
                    H2TlsSupport.setEnableRetransmissions(sslParameters, false);
                }

                applyParameters(sslEngine, sslParameters, H2TlsSupport.selectApplicationProtocols(attachment));

                initializeEngine(sslEngine);

                Log.d(TAG,"Enabled protocols: {} "+Arrays.asList(sslEngine.getEnabledProtocols()));
                Log.d(TAG,"Enabled cipher suites:{} "+Arrays.asList(sslEngine.getEnabledCipherSuites()));
            }

        }, new SSLSessionVerifier() {

            @Override
            public TlsDetails verify(final NamedEndpoint endpoint, final SSLEngine sslEngine) throws SSLException {
                verifySession(host.getHostName(), sslEngine.getSession());
                Log.i(TAG,"In verify" );
                final TlsDetails tlsDetails = createTlsDetails(sslEngine);
                final String negotiatedCipherSuite = sslEngine.getSession().getCipherSuite();
/*                if (tlsDetails != null && ApplicationProtocol.HTTP_2.id.equals(tlsDetails.getApplicationProtocol())) {
                    if (TlsCiphers.isH2Blacklisted(negotiatedCipherSuite)) {
                        throw new SSLHandshakeException("Cipher suite `" + negotiatedCipherSuite
                                + "` does not provide adequate security for HTTP/2");
                    }
                }*/
                return tlsDetails;
            }

        }, handshakeTimeout);
        Log.i(TAG,"StartTLS ended");
        return true;
    }

    void applyParameters(final SSLEngine sslEngine, final SSLParameters sslParameters, final String[] appProtocols) {
        Log.i(TAG,"apply parameters");
        H2TlsSupport.setApplicationProtocols(sslParameters, appProtocols);
        sslEngine.setSSLParameters(sslParameters);
    }

    TlsDetails createTlsDetails(final SSLEngine sslEngine) {
//        return tlsDetailsFactory != null ? tlsDetailsFactory.create(sslEngine) : null;
        return null;
    }

    protected void initializeEngine(final SSLEngine sslEngine) {
        Log.i(TAG,"initEngine");
    }

    protected void verifySession(
            final String hostname,
            final SSLSession sslsession) throws SSLException {
        tlsSessionValidator.verifySession(hostname, sslsession, hostnameVerifier);
    }
}
