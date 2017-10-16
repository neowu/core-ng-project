package core.framework.crypto;

import core.framework.util.ClasspathResources;
import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class SignatureTest {
    @Test
    void signWithJavaGeneratedPrivateKey() {
        Signature signature = new Signature();
        KeyPair keyPair = RSA.generateKeyPair();
        signature.privateKey(keyPair.getPrivate().getEncoded());
        signature.publicKey(keyPair.getPublic().getEncoded());

        String message = "test message";
        byte[] sign = signature.sign(Strings.bytes(message));
        boolean valid = signature.verify(Strings.bytes(message), sign);
        assertTrue(valid);
    }

    @Test
    void signWithOpenSSLGeneratedCert() {
        Signature signature = new Signature();
        byte[] cert = ClasspathResources.bytes("crypto-test/signature-cert.der");
        byte[] privateKey = ClasspathResources.bytes("crypto-test/signature-private.der");

        signature.privateKey(privateKey);
        signature.certificate(cert);
        String message = "test message";

        byte[] sign = signature.sign(Strings.bytes(message));
        boolean valid = signature.verify(Strings.bytes(message), sign);
        assertTrue(valid);
    }

    @Test
    void invalidSignature() {
        Signature signature = new Signature();
        byte[] cert = ClasspathResources.bytes("crypto-test/signature-cert-not-match.der");
        byte[] privateKey = ClasspathResources.bytes("crypto-test/signature-private.der");

        signature.privateKey(privateKey);
        signature.certificate(cert);
        String message = "test message";

        byte[] sign = signature.sign(Strings.bytes(message));
        boolean valid = signature.verify(Strings.bytes(message), sign);
        assertFalse(valid);
    }
}
