package core.framework.api.util;

import java.util.Base64;

/**
 * @author neo
 */
public final class Encodings {
    public static String base64(String text) {
        return base64(Strings.bytes(text));
    }

    public static String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] decodeBase64(String text) {
        return Base64.getDecoder().decode(text);
    }

    public static String base64URLSafe(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static byte[] decodeBase64URLSafe(String text) {
        return Base64.getUrlDecoder().decode(text);
    }
}
