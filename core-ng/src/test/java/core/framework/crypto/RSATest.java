package core.framework.crypto;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class RSATest {
    @Test
    void encryptAndDecrypt() {
        KeyPair keyPair = RSA.generateKeyPair();

        RSA rsa = new RSA();
        rsa.privateKey(keyPair.getPrivate().getEncoded());
        rsa.publicKey(keyPair.getPublic().getEncoded());

        String message = "test message";
        byte[] encryptedMessage = rsa.encrypt(Strings.bytes(message));
        byte[] decryptedMessage = rsa.decrypt(encryptedMessage);
        assertEquals(message, new String(decryptedMessage, StandardCharsets.UTF_8));
    }
}
