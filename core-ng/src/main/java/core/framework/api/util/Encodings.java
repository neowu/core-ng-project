package core.framework.api.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.BitSet;

/**
 * @author neo
 */
public final class Encodings {
    private static final byte[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    // refer to https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent,
    // refer to http://tools.ietf.org/html/rfc3986
    private static final BitSet URI_UNESCAPED = new BitSet(128);

    static {
        for (int i = 'a'; i <= 'z'; i++) {
            URI_UNESCAPED.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            URI_UNESCAPED.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            URI_UNESCAPED.set(i);
        }
        URI_UNESCAPED.set('-');
        URI_UNESCAPED.set('_');
        URI_UNESCAPED.set('.');
        URI_UNESCAPED.set('!');
        URI_UNESCAPED.set('~');
        URI_UNESCAPED.set('*');
        URI_UNESCAPED.set('\'');
        URI_UNESCAPED.set('(');
        URI_UNESCAPED.set(')');
    }

    // refer to impl from org.springframework.web.util.HierarchicalUriComponents#encodeUriComponent
    public static String uriComponent(String value) {
        byte[] bytes = Strings.bytes(value);
        int length = bytes.length;

        for (int i = 0; i < length; i++) {
            byte b1 = bytes[i];
            if (b1 < 0 || !URI_UNESCAPED.get(b1)) {   // the bytes java returned is signed, but we only need to check ascii (0-127)
                ByteBuf buffer = ByteBuf.newBuffer(length * 2);
                if (i > 0) buffer.put(bytes, 0, i);
                for (int j = i; j < length; j++) {
                    byte b2 = bytes[j];
                    if (b2 >= 0 && URI_UNESCAPED.get(b2)) {
                        buffer.put(b2);
                    } else {
                        buffer.put((byte) '%');
                        buffer.put(HEX_CHARS[(b2 >> 4) & 0xF]);
                        buffer.put(HEX_CHARS[b2 & 0xF]);
                    }
                }
                return buffer.text(StandardCharsets.US_ASCII);
            }
        }
        return value;
    }

    // refer to impl from org.springframework.web.util.UriUtils#decode
    public static String decodeURIComponent(String value) {
        int length = value.length();
        int index = 0;
        for (; index < length; index++) {
            int ch = value.charAt(index);
            if (ch == '%') break;
        }
        if (index == length) return value;
        ByteBuf buffer = ByteBuf.newBuffer(length);
        for (int i = 0; i < index; i++) buffer.put((byte) value.charAt(i));
        for (; index < length; index++) {
            int ch = value.charAt(index);
            if (ch == '%') {
                if ((index + 2) >= length) throw new IllegalArgumentException("invalid uri encoding, value=" + value.substring(index));
                char hex1 = value.charAt(index + 1);
                char hex2 = value.charAt(index + 2);
                int u = Character.digit(hex1, 16);
                int l = Character.digit(hex2, 16);
                if (u == -1 || l == -1) throw new IllegalArgumentException("invalid uri encoding, value=" + value.substring(index));
                buffer.put((byte) ((u << 4) + l));
                index += 2;
            } else {
                buffer.put((byte) ch);
            }
        }
        return buffer.text();
    }

    public static String base64(String value) {
        return base64(Strings.bytes(value));
    }

    public static String base64(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    public static byte[] decodeBase64(String value) {
        return Base64.getDecoder().decode(value);
    }

    public static String base64URLSafe(byte[] value) {
        return Base64.getUrlEncoder().encodeToString(value);
    }

    public static byte[] decodeBase64URLSafe(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
