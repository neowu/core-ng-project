package core.framework.api.crypto;

import core.framework.api.util.Charsets;

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
            KeyGenerator generator = KeyGenerator.getInstance(algorithm(hash));
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

    public byte[] digest(String message) {
        if (key == null) throw new Error("key must not be null");
        try {
            String algorithm = algorithm(hash);
            Mac mac = Mac.getInstance(algorithm);

            SecretKey secretKey = new SecretKeySpec(key, algorithm);
            mac.init(secretKey);
            return mac.doFinal(message.getBytes(Charsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new Error(e);
        }
    }

    private static String algorithm(Hash hash) {
        return "Hmac" + hash.value;
    }

    public enum Hash {
        MD5("MD5"), SHA1("SHA1"), SHA256("SHA256"), SHA512("SHA512");
        final String value;

        Hash(String value) {
            this.value = value;
        }
    }
}
