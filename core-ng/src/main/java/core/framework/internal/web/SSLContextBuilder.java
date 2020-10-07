package core.framework.internal.web;

import core.framework.internal.http.PEM;

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
public class SSLContextBuilder {
    // generate cert/key by "openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -nodes -days 3650"
    private static final String CERT = """
            -----BEGIN CERTIFICATE-----
            MIIEoDCCAogCCQC5Kos+icdPjzANBgkqhkiG9w0BAQsFADASMRAwDgYDVQQDDAd1
            bmtub3duMB4XDTIwMDQwMTE5NTA0NloXDTMwMDMzMDE5NTA0NlowEjEQMA4GA1UE
            AwwHdW5rbm93bjCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAOs1QKoL
            rmz3QcOLlhZl3Wpv2lDaxTt3ZuZalL9ISx9O0MTz5jvXW3uJWbn4ceQTxGQkJ9aL
            QkIyR73ZK4P0RvGZY9zLzPsM84FS4WXS0UEDYw39TTMAYy/XPs/DmThPZAR1fttG
            LHtuTyiWZ4IwqXcVKzwPVROiSMVTcfMn/Tf0pSpppnE6HqcicPmoTFK6MwAZ+AAw
            ewE3c7YpnrPlG8U3ll7WXVnenAEtVO4yu3jpPUEavb9KfuEbyPaXO3hKntGsER7O
            Y2hQ/2zEozaHLVsXiJ5Chsdgrow7e8fs2VZU5WbuBWE6i3sijjPPqwTYtGP8ABPf
            vUI6NUhMUXa/Dz00A0W04Yx9DSUd/dekTrm126ewp6zKH3v6RiluvsOC9gTjdjc7
            qmuoO32M1NWVYx0RhWKL5HmZASWA7nGGmHS7PSqu3csz8I2vNA5SBFh5+WSwtpoh
            3ta6MVkneETNWH9K5UqVZ9DDNtFGghGgvqcEyoQurPhKrIcq9oj1clR8+B1X7clY
            bwSHN1iRj0ThlFbFAO5j+5apT+FCbQ3qW1OWrvMI01fqjdzRBlUhVdPfYK6awdOO
            kfzmozOKgNHCv/83c7PUKpMCQtpJFgljNckctM1TGdn9TAnYMXWrAfxcakUblJ0q
            HbdDR8Qvubrq8LPE7D9VX5/vIOvdi+Re3f3XAgMBAAEwDQYJKoZIhvcNAQELBQAD
            ggIBAD1pPUBPDi/QTcFfFwGGBwkwygCaAjaKwPSVEXy1YiMiU8kVKfExzZG8gjGy
            4sYCsHAmkdHe5DLr0BCPmkTq71zSarsEPeLdlA0D+pIQwX4GWDCZyPzbh7IRPWC8
            A2DIXvUObsYR/J/umR7W4voWKky88UdHxDnYrzcRLhnKJUfpsfUiF8jOLgwMxIDE
            3k/+5DqCoVtI2zjmGCahPJ2Rsnz8WKosBRvHCzVWlcytOWFzAyyP3f0LfXGGX04l
            NvXcqTxcHm2F+k56y2pHGir0IyR0zMMi1m2Hjr1kLCP0aSlrrGilo4bQrD1Qzz9o
            oxcCbhfVBlVe9BWtVPRAen0/GVKxM2mpO2zZO6ckQqdQYPRoVcmvi9v8Hv4Frwlj
            8IeXq0ynzklaepu7jLEAuAOZXGQjZm0bTFSNE8HImwTh+Wc0useMGrNo44t03/+x
            eEN07j16rE8iMyG1TKSr0GzvpZjwgAthPSmCyKvJi1U8fT26ZStQXIFErKZ4Izk5
            P9hEBzjd6+qe+9CxSWO0fM1hHCu7g2ADYjDUuV5VoUVtkZWpiPnM+CY/rmdfwElK
            Rc951Uzxq+tSNopgkDbeMLc78oCneiIkG5kRtGSg0cq9F6PvAsSq/wHH2Pt/CeLG
            T6iN3OqSp9rSOWhhEjIdDLmv47tBBg006TxZdgFHItcYxBj2
            -----END CERTIFICATE-----
            """;
    private static final String KEY = """
            -----BEGIN PRIVATE KEY-----
            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
            TrR8UkBExY7UIS0B+fiT0pzlru06BA==
            -----END PRIVATE KEY-----
            """;

    public SSLContext build() {
        try {
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(PEM.decode(KEY)));
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(PEM.decode(CERT)));

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
