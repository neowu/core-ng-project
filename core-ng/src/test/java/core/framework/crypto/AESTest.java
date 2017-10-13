package core.framework.crypto;

import core.framework.util.Charsets;
import core.framework.util.Strings;
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
        byte[] cipherText = aes.encrypt(Strings.bytes(message));
        byte[] plainBytes = aes.decrypt(cipherText);
        String plainText = new String(plainBytes, Charsets.UTF_8);

        Assert.assertEquals(message, plainText);
    }
}
