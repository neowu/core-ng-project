package core.framework.api.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.net.URLCodec;

import java.util.Base64;
import java.util.BitSet;

/**
 * @author neo
 */
public final class Encodings {
    public static String hex(byte[] bytes) {
        return new String(Hex.encodeHex(bytes));
    }

    public static byte[] decodeHex(String text) {
        try {
            return Hex.decodeHex(text.toCharArray());
        } catch (DecoderException e) {
            throw new Error(e);
        }
    }

    public static String base64(String text) {
        return base64(Strings.bytes(text));
    }

    public static String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] decodeBase64(String text) {
        return decodeBase64(Strings.bytes(text));
    }

    public static byte[] decodeBase64(byte[] bytes) {
        return Base64.getDecoder().decode(bytes);
    }

    public static String base64URLSafe(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    // refer to org.apache.commons.codec.net.URLCodec.WWW_FORM_URL, not including space, url path requires space be encoded as %20
    private static final BitSet URL_PATH = new BitSet(256);

    static {
        for (int i = 'a'; i <= 'z'; i++) {
            URL_PATH.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            URL_PATH.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            URL_PATH.set(i);
        }
        URL_PATH.set('-');
        URL_PATH.set('_');
        URL_PATH.set('.');
        URL_PATH.set('*');
    }

    // url encoding is for queryString, url path encoding is for url path, the difference is queryString uses + for space, url path uses %20 for space
    public static String urlPath(String text) {
        return new String(URLCodec.encodeUrl(URL_PATH, Strings.bytes(text)), Charsets.UTF_8);
    }

    public static String url(String text) {
        return new String(URLCodec.encodeUrl(null, Strings.bytes(text)), Charsets.UTF_8);
    }

    public static String decodeURL(String text) {
        try {
            return new String(URLCodec.decodeUrl(Strings.bytes(text)), Charsets.UTF_8);
        } catch (DecoderException e) {
            throw new Error(e);
        }
    }
}
