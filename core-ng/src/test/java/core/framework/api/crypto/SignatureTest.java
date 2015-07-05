package core.framework.api.crypto;

import core.framework.api.util.ClasspathResources;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;

/**
 * @author neo
 */
public class SignatureTest {
    @Test
    public void signWithJavaGeneratedPrivateKey() {
        Signature signature = new Signature();
        KeyPair keyPair = RSA.generateKeyPair();
        signature.privateKey(keyPair.getPrivate().getEncoded());
        signature.publicKey(keyPair.getPublic().getEncoded());

        String message = "test message";
        byte[] sign = signature.sign(message.getBytes());
        boolean valid = signature.verify(message.getBytes(), sign);

        Assert.assertTrue(valid);
    }

    @Test
    public void signWithOpenSSLGeneratedCert() {
        Signature signature = new Signature();
        byte[] cert = ClasspathResources.bytes("crypto-test/signature-cert.der");
        byte[] privateKey = ClasspathResources.bytes("crypto-test/signature-private.der");

        signature.privateKey(privateKey);
        signature.certificate(cert);
        String message = "test message";

        byte[] sign = signature.sign(message.getBytes());

        boolean valid = signature.verify(message.getBytes(), sign);
        Assert.assertTrue(valid);
    }

    @Test
    public void invalidSignature() {
        Signature signature = new Signature();
        byte[] cert = ClasspathResources.bytes("crypto-test/signature-cert-not-match.der");
        byte[] privateKey = ClasspathResources.bytes("crypto-test/signature-private.der");

        signature.privateKey(privateKey);
        signature.certificate(cert);
        String message = "test message";

        byte[] sign = signature.sign(message.getBytes());
        boolean valid = signature.verify(message.getBytes(), sign);
        Assert.assertFalse(valid);
    }
}
