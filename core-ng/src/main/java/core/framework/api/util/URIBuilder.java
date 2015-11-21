package core.framework.api.util;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * @author neo
 */
public final class URIBuilder {
    /*
       pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
       pct-encoded   = "%" HEXDIG HEXDIG
       unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
       sub-delims    = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
       gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"

       segment       = *pchar
       query         = *( pchar / "/" / "?" )
       fragment      = *( pchar / "/" / "?" )
    */
    private static final BitSet P_CHAR = new BitSet(128);
    private static final BitSet QUERY_PARAM = new BitSet(128);
    private static final BitSet FRAGMENT = new BitSet(128);

    static {
        // unreserved
        for (int i = 'a'; i <= 'z'; i++) {
            P_CHAR.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            P_CHAR.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            P_CHAR.set(i);
        }
        P_CHAR.set('-');
        P_CHAR.set('.');
        P_CHAR.set('_');
        P_CHAR.set('~');
        // sub-delims
        P_CHAR.set('!');
        P_CHAR.set('$');
        P_CHAR.set('&');
        P_CHAR.set('\'');
        P_CHAR.set('(');
        P_CHAR.set(')');
        P_CHAR.set('*');
        P_CHAR.set('+');
        P_CHAR.set(',');
        P_CHAR.set(';');
        P_CHAR.set('=');
        // ":" and "@"
        P_CHAR.set(':');
        P_CHAR.set('@');

        FRAGMENT.or(P_CHAR);
        FRAGMENT.set('/');
        FRAGMENT.set('?');

        QUERY_PARAM.or(FRAGMENT);
        QUERY_PARAM.clear('+');   // this is not defined in rfc3986, but query param can not contains +/=/& (not like query), another wise it will mislead query string parsing
        QUERY_PARAM.clear('=');
        QUERY_PARAM.clear('&');
    }

    static String encodePathSegment(String segment) {
        return encode(P_CHAR, segment);
    }

    static String encodeQueryParam(String param) {
        return encode(QUERY_PARAM, param);
    }

    static String encodeFragment(String fragment) {
        return encode(FRAGMENT, fragment);
    }

    // refer to http://www.ietf.org/rfc/rfc3986.txt, org.springframework.web.util.HierarchicalUriComponents#encodeUriComponent
    static String encode(BitSet safeChars, String value) {
        byte[] bytes = Strings.bytes(value);
        int length = bytes.length;
        int index = 0;
        for (; index < length; index++) {
            byte b = bytes[index];
            if (b < 0 || !safeChars.get(b)) {   // the bytes java returned is signed, but we only need to check ascii (0-127)
                break;
            }
        }
        if (index == length) return value;
        ByteBuf buffer = ByteBuf.newBuffer(bytes.length * 2);
        if (index > 0) buffer.put(bytes, 0, index);
        for (; index < length; index++) {
            byte b = bytes[index];
            if (b >= 0 && safeChars.get(b)) {
                buffer.put(b);
            } else {
                buffer.put((byte) '%');
                char hex1 = toUpper(Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = toUpper(Character.forDigit(b & 0xF, 16));
                buffer.put((byte) hex1);
                buffer.put((byte) hex2);
            }
        }
        return buffer.text(StandardCharsets.US_ASCII);
    }

    private static char toUpper(char ch) {
        if (ch >= 'a' && ch <= 'z') return (char) (ch & 0x5F);
        return ch;
    }

    private final StringBuilder uri;
    private boolean queryStarted;

    public URIBuilder() {
        uri = new StringBuilder();
    }

    public URIBuilder(String prefix) {
        uri = new StringBuilder(prefix);
        queryStarted = prefix.indexOf('?') > 0;
    }

    public URIBuilder fragment(String fragment) {
        uri.append('#').append(encodeFragment(fragment));
        return this;
    }

    public URIBuilder addPath(String segment) {
        if (queryStarted) throw Exceptions.error("path segment must not be added after query, uri={}", uri.toString());
        if (uri.length() > 0 && uri.charAt(uri.length() - 1) != '/') uri.append('/');
        uri.append(encode(P_CHAR, segment));
        return this;
    }

    public URIBuilder addQueryParam(String name, String value) {
        uri.append(queryStarted ? '&' : '?').append(encodeQueryParam(name)).append('=').append(encodeQueryParam(value));
        queryStarted = true;
        return this;
    }

    public String toURI() {
        return uri.toString();
    }
}
