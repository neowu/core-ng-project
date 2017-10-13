package core.framework.crypto;

import core.framework.util.Charsets;
import core.framework.util.Encodings;

import java.security.KeyPair;

/**
 * Util to decrypt sensitive data, such as db user password
 * <p>
 * Using RSA+Base64 with PEM encoded keys, and all string based,
 * <p>
 * Since we use 2048 RSA key, so the max length of text can be encrypted is 256
 *
 * @author neo
 */
public final class Password {
    public static String encrypt(String plainText, String publicKey) {
        RSA rsa = new RSA();
        rsa.publicKey(PEM.fromPEM(publicKey));
        byte[] encryptedBytes = rsa.encrypt(plainText.getBytes(Charsets.UTF_8));
        return Encodings.base64(encryptedBytes);
    }

    public static String decrypt(String encryptedText, String privateKey) {
        RSA rsa = new RSA();
        rsa.privateKey(PEM.fromPEM(privateKey));
        byte[] encryptedBytes = Encodings.decodeBase64(encryptedText);
        byte[] plainText = rsa.decrypt(encryptedBytes);
        return new String(plainText, Charsets.UTF_8);
    }

    public static String[] generateKeyPair() {
        KeyPair keyPair = RSA.generateKeyPair();
        String publicKey = PEM.toPEM("RSA PUBLIC KEY", keyPair.getPublic().getEncoded());
        String privateKey = PEM.toPEM("RSA PRIVATE KEY", keyPair.getPrivate().getEncoded());
        return new String[]{publicKey, privateKey};
    }
}
