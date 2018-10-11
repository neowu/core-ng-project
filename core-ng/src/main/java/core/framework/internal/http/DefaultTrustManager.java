package core.framework.internal.http;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.X509Certificate;

/**
 * @author neo
 */
public class DefaultTrustManager extends X509ExtendedTrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
    }

    // if impled X509ExtendedTrustManager, sslContext won't wrap sun.security.ssl.AbstractTrustManagerWrapper
    // and there is no need to set "jdk.internal.httpclient.disableHostnameVerification" system property
    // refer to sun.security.ssl.SSLContextImpl.chooseTrustManager
    // refer to jdk.internal.net.http.common.Utils.hostnameVerificationDisabledValue
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}

