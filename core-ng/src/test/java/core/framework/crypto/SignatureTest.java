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
        KeyPair keyPair = RSA.generateKeyPair();
        String message = "test message";

        Signature signature = new Signature();
        signature.privateKey(keyPair.getPrivate().getEncoded());

        byte[] sign = signature.sign(Strings.bytes(message));

        signature = new Signature();
        signature.publicKey(keyPair.getPublic().getEncoded());

        assertTrue(signature.verify(Strings.bytes(message), sign));
    }

    @Test
    void signWithOpenSSLGeneratedCert() {
        String message = "test message";

        Signature signature = new Signature();
        signature.privateKey(PEM.fromPEM(ClasspathResources.text("crypto-test/signature-private.pem")));

        byte[] sign = signature.sign(Strings.bytes(message));

        signature = new Signature();
        signature.certificate(PEM.fromPEM(ClasspathResources.text("crypto-test/signature-cert.pem")));

        assertTrue(signature.verify(Strings.bytes(message), sign));
    }

    @Test
    void invalidSignature() {
        String message = "test message";

        Signature signature = new Signature();
        signature.privateKey(PEM.fromPEM(ClasspathResources.text("crypto-test/signature-private.pem")));

        byte[] sign = signature.sign(Strings.bytes(message));

        signature = new Signature();
        signature.certificate(PEM.fromPEM(ClasspathResources.text("crypto-test/signature-cert-not-match.pem")));

        assertFalse(signature.verify(Strings.bytes(message), sign));
    }
}
