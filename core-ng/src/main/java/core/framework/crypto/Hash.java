package core.framework.crypto;

import core.framework.util.Strings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author neo
 */
public final class Hash {
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    // md5/sha1 are not considered as secure algorithm, only used on legacy or security insensitive cases, like checksum or masking
    public static String md5Hex(byte[] value) {
        return hash(value, "MD5");
    }

    public static String md5Hex(String value) {
        return md5Hex(Strings.bytes(value));
    }

    public static String sha1Hex(byte[] value) {
        return hash(value, "SHA1");
    }

    public static String sha1Hex(String value) {
        return sha1Hex(Strings.bytes(value));
    }

    public static String sha256Hex(byte[] value) {
        return hash(value, "SHA-256");
    }

    public static String sha256Hex(String value) {
        return sha256Hex(Strings.bytes(value));
    }

    private static String hash(byte[] value, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);    // lgtm[java/weak-cryptographic-algorithm]
            byte[] digest = md.digest(value);
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
