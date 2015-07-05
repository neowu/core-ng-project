package core.framework.api.crypto;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class AESTest {
    @Test
    public void encryptAndDecrypt() {
        byte[] key = AES.generateKey(128);
        AES aes = new AES(key);

        String message = "test-message";
        byte[] cipherText = aes.encrypt(message.getBytes());
        byte[] plainBytes = aes.decrypt(cipherText);
        String plainText = new String(plainBytes);

        Assert.assertEquals(message, plainText);
    }
}
