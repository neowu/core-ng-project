package core.framework.crypto;

import core.framework.util.ClasspathResources;
import core.framework.util.Strings;
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
        byte[] sign = signature.sign(Strings.bytes(message));
        boolean valid = signature.verify(Strings.bytes(message), sign);

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

        byte[] sign = signature.sign(Strings.bytes(message));

        boolean valid = signature.verify(Strings.bytes(message), sign);
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

        byte[] sign = signature.sign(Strings.bytes(message));
        boolean valid = signature.verify(Strings.bytes(message), sign);
        Assert.assertFalse(valid);
    }
}
