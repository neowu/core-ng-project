package core.framework.api.util;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * @author neo
 */
public final class URIBuilder {
    private static final byte[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

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

        // there was P_CHAR.set('+')
        // due to common mis-understanding, e.g. undertow/AWS S3, is implemented to encode '+' as part of url fragment, we have to encode it
        // in theory only QUERY_PARAM needs to clear '+'

        P_CHAR.set(',');
        P_CHAR.set(';');
        P_CHAR.set('=');
        // ":" and "@"
        P_CHAR.set(':');
        P_CHAR.set('@');

        FRAGMENT.or(P_CHAR);
        FRAGMENT.set('+');  // '+' should be P_CHAR, but since we have to keep compatible with other bad impl, so only put '+' in fragment
        FRAGMENT.set('/');
        FRAGMENT.set('?');

        QUERY_PARAM.or(P_CHAR);
        QUERY_PARAM.set('/');
        QUERY_PARAM.set('?');
        QUERY_PARAM.clear('='); // this is not defined in rfc3986, but query param can not contains +/=/& (not like query), another wise it will mislead query string parsing
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

        for (int i = 0; i < length; i++) {
            byte b1 = bytes[i];
            if (b1 < 0 || !safeChars.get(b1)) {   // the bytes java returned is signed, but we only need to check ascii (0-127)
                ByteBuf buffer = ByteBuf.newBuffer(length * 2);
                if (i > 0) buffer.put(bytes, 0, i);
                for (int j = i; j < length; j++) {
                    byte b2 = bytes[j];
                    if (b2 >= 0 && safeChars.get(b2)) {
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
