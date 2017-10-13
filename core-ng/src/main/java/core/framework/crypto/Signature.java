package core.framework.crypto;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Generate private key:
 * openssl genrsa -out private.pem 1024
 * openssl pkcs8 -topk8 -inform PEM -in private.pem -outform DER -out private.der -nocrypt
 * Generate cert:
 * openssl req -new -x509 -keyform PEM -key private.pem -outform DER -out cert.der
 *
 * @author neo
 */
public final class Signature {
    private static final String ALGORITHM_SHA1_WITH_RSA = "SHA1withRSA";
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public boolean verify(byte[] message, byte[] signatureValue) {
        try {
            java.security.Signature signature = java.security.Signature.getInstance(ALGORITHM_SHA1_WITH_RSA);
            signature.initVerify(publicKey);
            signature.update(message);
            return signature.verify(signatureValue);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new Error(e);
        }
    }

    public byte[] sign(byte[] message) {
        try {
            java.security.Signature signature = java.security.Signature.getInstance(ALGORITHM_SHA1_WITH_RSA);
            signature.initSign(privateKey);
            signature.update(message);
            return signature.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new Error(e);
        }
    }

    public Signature certificate(byte[] certificateValue) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
            Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(certificateValue));
            publicKey = certificate.getPublicKey();
            return this;
        } catch (CertificateException e) {
            throw new Error(e);
        }
    }

    public Signature publicKey(byte[] publicKeyValue) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyValue);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA.ALGORITHM_RSA);
            publicKey = keyFactory.generatePublic(keySpec);
            return this;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new Error(e);
        }
    }

    public Signature privateKey(byte[] privateKeyValue) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyValue);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA.ALGORITHM_RSA);
            privateKey = keyFactory.generatePrivate(keySpec);
            return this;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new Error(e);
        }
    }
}
