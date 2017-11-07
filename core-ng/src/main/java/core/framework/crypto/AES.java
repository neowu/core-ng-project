package core.framework.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

/**
 * @author neo
 */
public final class AES {
    private static final String ALGORITHM_AES = "AES";

    /**
     * generate the AES key, for using AES256 requires to update jdk with JCE Unlimited Strength Jurisdiction Policy Files
     *
     * @param keySize the size, use 128 or 256 for AES128/AES256
     * @return the key content
     */
    public static byte[] generateKey(int keySize) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM_AES);
            generator.init(keySize);
            return generator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    private final byte[] key;

    public AES(byte[] key) {
        this.key = key;
    }

    public byte[] encrypt(byte[] plainMessage) {
        try {
            Cipher cipher = createCipher(ENCRYPT_MODE);
            return cipher.doFinal(plainMessage);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new Error(e);
        }
    }

    public byte[] decrypt(byte[] encryptedMessage) {
        try {
            Cipher cipher = createCipher(DECRYPT_MODE);
            return cipher.doFinal(encryptedMessage);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new Error(e);
        }
    }

    private Cipher createCipher(int encryptMode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_AES);
        Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
        cipher.init(encryptMode, keySpec, new SecureRandom());
        return cipher;
    }
}
