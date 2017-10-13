package core.framework.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * http://en.wikipedia.org/wiki/Message_authentication_code
 *
 * @author neo
 */
public final class HMAC {
    public static byte[] generateKey(Hash hash) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(hash.algorithm);
            generator.init(128);
            return generator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    private final byte[] key;
    private final Hash hash;

    public HMAC(byte[] key, Hash hash) {
        this.key = key;
        this.hash = hash;
    }

    public byte[] digest(byte[] message) {
        if (key == null) throw new Error("key must not be null");
        try {
            Mac mac = Mac.getInstance(hash.algorithm);
            SecretKey secretKey = new SecretKeySpec(key, hash.algorithm);
            mac.init(secretKey);
            return mac.doFinal(message);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new Error(e);
        }
    }

    public enum Hash {
        MD5("HmacMD5"), SHA1("HmacSHA1"), SHA256("HmacSHA256"), SHA512("HmacSHA512");
        final String algorithm;

        Hash(String algorithm) {
            this.algorithm = algorithm;
        }
    }
}
