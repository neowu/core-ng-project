package core.framework.impl.web;

import core.framework.crypto.PEM;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author neo
 */
class SSLContextBuilder {
    SSLContext build() {
        String cert = "-----BEGIN CERTIFICATE-----\n"
                + "MIICITCCAYoCCQCYd6FYSuVDODANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJV\n"
                + "UzEQMA4GA1UECAwHdW5rbm93bjEQMA4GA1UEBwwHdW5rbm93bjEQMA4GA1UECgwH\n"
                + "dW5rbm93bjEQMA4GA1UEAwwHdW5rbm93bjAeFw0xNDA0MjQxODE2MDFaFw0yNDA0\n"
                + "MjExODE2MDFaMFUxCzAJBgNVBAYTAlVTMRAwDgYDVQQIDAd1bmtub3duMRAwDgYD\n"
                + "VQQHDAd1bmtub3duMRAwDgYDVQQKDAd1bmtub3duMRAwDgYDVQQDDAd1bmtub3du\n"
                + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDG29Nnh2yXmHOldHT15291trI3\n"
                + "2RIax/rMfnByZBwtYKPt6G/+f3JZ4T4n/eerwSg+GwqrMPEn56GHkQoEkVynx76I\n"
                + "Ds+3WSHeBpNYV3dofl/sKkkpUxLuCZ4hKKn+XGswi9zeC8FBlRiQj4T6jE13WGLi\n"
                + "zGEeSWmvKK49XdlxJwIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAFweMGjR7ARe5FCT\n"
                + "YLxZlclDuT4N3yvYf8TUExNYYjG7eL1mGDvfkbZJ//daUsAeoHRTfFIi0sPAOAMJ\n"
                + "Y0L4ejwKFziPxGXVJE5MKVQBrNu4Zm5I+1SwSMI0A1PBMXSLWaqn6j9D5vchsVgs\n"
                + "2H9+2fvrTrHGAI8L7qHzi+ODImYf\n"
                + "-----END CERTIFICATE-----";
        String privateKey1 = "-----BEGIN PRIVATE KEY-----\n"
                + "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMbb02eHbJeYc6V0\n"
                + "dPXnb3W2sjfZEhrH+sx+cHJkHC1go+3ob/5/clnhPif956vBKD4bCqsw8SfnoYeR\n"
                + "CgSRXKfHvogOz7dZId4Gk1hXd2h+X+wqSSlTEu4JniEoqf5cazCL3N4LwUGVGJCP\n"
                + "hPqMTXdYYuLMYR5Jaa8orj1d2XEnAgMBAAECgYEAjAYQJw8pvNkhXXjSPrDXQBkE\n"
                + "BuU3pVn5VHMXtMSfPqiU5ZnM+nQ9TeKXxMs5jSw2rPyXl5GfzYyBphbP6gV9Kn1j\n"
                + "5cLtWI9xc+M0OOHP9NbSUCGLS6MkjR7zRe5Mg6ApdYx6Lx8FLosFQO4FX/7Mk8/x\n"
                + "Pa/m2Kb0hKQDYnn9QdkCQQDlqt7cF1H0VmjI0AeTd8qkIR8PQqLXtRp0pGER25b3\n"
                + "Oz9+GoMGZYuGFINGCBDWw34AMCc4EAmezLQ5/RunE5pVAkEA3aiqaJBKYlkmbhNP\n"
                + "T95FyCScnDaLGyfFMcueOsYSbRj3LEhwyy+C3YRG38BIE5aCCCLyVnyred88cf7M\n"
                + "pCERiwJAI0kmZmA62jRwcvHrSA/ulVr1X63YQRX1E5ixxUGcpy12KtS97rypPBdo\n"
                + "t9jDZYuxjyvWyrlEER7YTdSCbCAJ5QJABTOqHB4WwMwazMaDO/qZZKMHUdst1ItQ\n"
                + "Y2TF59cyI4FMe6uPihUpWw15pFKc3mjP0GURjtoKJCgLARnbr5ZfFQJAaU5QJpr3\n"
                + "PQ29X73wEdm3t93e3lXCK6ez1gMik1fXXR2hCoEvzXyMVAfEaCg494pIApfwTtNL\n"
                + "nX1T2cCQuFQrYA==\n"
                + "-----END PRIVATE KEY-----\n";
        try {
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(PEM.fromPEM(privateKey1)));
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(PEM.fromPEM(cert)));

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setKeyEntry("default", privateKey, new char[0], new Certificate[]{certificate});

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, new char[0]);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), null, null);
            return context;
        } catch (KeyStoreException | IOException | CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyManagementException | InvalidKeySpecException e) {
            throw new Error(e);
        }
    }
}
