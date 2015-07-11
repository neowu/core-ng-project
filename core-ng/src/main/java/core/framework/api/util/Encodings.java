package core.framework.api.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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
        return base64(text.getBytes(Charsets.UTF_8));
    }

    public static String base64(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes), Charsets.UTF_8);
    }

    public static byte[] decodeBase64(String base64Text) {
        return decodeBase64(base64Text.getBytes(Charsets.UTF_8));
    }

    public static byte[] decodeBase64(byte[] base64Bytes) {
        return Base64.decodeBase64(base64Bytes);
    }

    public static String base64URLSafe(byte[] bytes) {
        return new String(Base64.encodeBase64URLSafe(bytes), Charsets.UTF_8);
    }

    public static String url(String text) {
        try {
            return URLEncoder.encode(text, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static String decodeURL(String text) {
        try {
            return URLDecoder.decode(text, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
