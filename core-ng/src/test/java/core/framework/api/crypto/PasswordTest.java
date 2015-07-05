package core.framework.api.crypto;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class PasswordTest {
    @Test
    public void encryptAndDecrypt() {
        String[] keyPair = Password.generateKeyPair();
        String encryptedText = Password.encrypt("test", keyPair[0]);
        String decryptedText = Password.decrypt(encryptedText, keyPair[1]);
        Assert.assertEquals("test", decryptedText);
    }
}
