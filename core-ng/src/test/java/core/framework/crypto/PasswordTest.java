package core.framework.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class PasswordTest {
    @Test
    void encryptAndDecrypt() {
        String[] keyPair = Password.generateKeyPair();
        String encryptedText = Password.encrypt("test", keyPair[0]);
        String decryptedText = Password.decrypt(encryptedText, keyPair[1]);
        assertEquals("test", decryptedText);
    }
}
