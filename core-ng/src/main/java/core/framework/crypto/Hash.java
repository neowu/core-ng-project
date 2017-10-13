package core.framework.crypto;

import core.framework.util.Strings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author neo
 */
public final class Hash {
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String md5Hex(String text) {
        return hash(text, "MD5");
    }

    public static String sha1Hex(String text) {
        return hash(text, "SHA1");
    }

    public static String sha256Hex(String text) {
        return hash(text, "SHA-256");
    }

    private static String hash(String text, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(Strings.bytes(text));
            return hex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    private static String hex(byte[] bytes) {
        char[] chars = new char[bytes.length << 1];
        int index = 0;
        for (byte b : bytes) {  // two characters form the hex value.
            chars[index++] = HEX_CHARS[(b >> 4) & 0xF];
            chars[index++] = HEX_CHARS[b & 0xF];
        }
        return new String(chars);
    }
}
