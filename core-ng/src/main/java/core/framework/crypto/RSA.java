package core.framework.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author neo
 */
public final class RSA {
    static final String ALGORITHM_RSA = "RSA";

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM_RSA);
            generator.initialize(2048);
            return generator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public byte[] decrypt(byte[] encryptedMessage) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedMessage);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Error("failed to decrypt message, please check private key and message", e);
        } catch (InvalidKeyException e) {
            throw new Error(e);
        }
    }

    public byte[] encrypt(byte[] plainMessage) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plainMessage);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new Error(e);
        }
    }

    public void privateKey(byte[] privateKeyValue) {
        try {
            var keySpec = new PKCS8EncodedKeySpec(privateKeyValue);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new Error(e);
        }
    }

    public void publicKey(byte[] publicKeyValue) {
        try {
            var keySpec = new X509EncodedKeySpec(publicKeyValue);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new Error(e);
        }
    }
}
