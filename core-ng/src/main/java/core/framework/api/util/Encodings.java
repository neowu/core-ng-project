package core.framework.api.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.net.URLCodec;

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
        return new String(Base64.encodeBase64(bytes), Charsets.UTF_8);
    }

    public static byte[] decodeBase64(String base64Text) {
        return decodeBase64(Strings.bytes(base64Text));
    }

    public static byte[] decodeBase64(byte[] base64Bytes) {
        return Base64.decodeBase64(base64Bytes);
    }

    public static String base64URLSafe(byte[] bytes) {
        return new String(Base64.encodeBase64URLSafe(bytes), Charsets.UTF_8);
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
