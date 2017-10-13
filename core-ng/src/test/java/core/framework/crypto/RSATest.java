package core.framework.crypto;

import core.framework.util.Charsets;
import core.framework.util.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;

/**
 * @author neo
 */
public class RSATest {
    @Test
    public void encryptAndDecrypt() {
        KeyPair keyPair = RSA.generateKeyPair();

        RSA rsa = new RSA();
        rsa.privateKey(keyPair.getPrivate().getEncoded());
        rsa.publicKey(keyPair.getPublic().getEncoded());

        String message = "test message";
        byte[] encryptedMessage = rsa.encrypt(Strings.bytes(message));
        byte[] decryptedMessage = rsa.decrypt(encryptedMessage);
        Assert.assertEquals(message, new String(decryptedMessage, Charsets.UTF_8));
    }
}
