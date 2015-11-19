package core.framework.api.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * @author neo
 */
public final class URIBuilder {
    /*
       pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
       query         = *( pchar / "/" / "?" )
       fragment      = *( pchar / "/" / "?" )
       pct-encoded   = "%" HEXDIG HEXDIG
       unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
       sub-delims    = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
       gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"

       segment       = *pchar
       query         = *( pchar / "/" / "?" )
    */
    static final BitSet P_CHAR = new BitSet(256);
    static final BitSet QUERY = new BitSet(256);
    static final BitSet GEN_DELIMS = new BitSet(8);

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

        P_CHAR.stream().forEach(QUERY::set);
        QUERY.set('/');
        QUERY.set('?');

        GEN_DELIMS.set(':');
        GEN_DELIMS.set('/');
        GEN_DELIMS.set('?');
        GEN_DELIMS.set('#');
        GEN_DELIMS.set('[');
        GEN_DELIMS.set(']');
        GEN_DELIMS.set('@');
    }

    public static boolean isValidURIChar(char ch) {
        return P_CHAR.get(ch) || GEN_DELIMS.get(ch);
    }

    public static String encodePathSegment(String segment) {
        return encode(P_CHAR, segment);
    }

    // refer to http://www.ietf.org/rfc/rfc3986.txt
    // refer to org.springframework.web.util.HierarchicalUriComponents#encodeUriComponent
    static String encode(BitSet safeChars, String value) {
        byte[] bytes = Strings.bytes(value);
        ByteArrayOutputStream stream = new ByteArrayOutputStream(bytes.length * 2);
        boolean changed = false;
        for (byte b : bytes) {
            int ch = b & 0xFF;
            if (safeChars.get(ch)) {
                stream.write(ch);
            } else {
                stream.write('%');
                char hex1 = toUpper(Character.forDigit((ch >> 4) & 0xF, 16));
                char hex2 = toUpper(Character.forDigit(ch & 0xF, 16));
                stream.write(hex1);
                stream.write(hex2);

                changed = true;
            }
        }
        return changed ? new String(stream.toByteArray(), StandardCharsets.US_ASCII) : value;    // return original string to help GC
    }

    private static char toUpper(char ch) {
        if (ch >= 'a' && ch <= 'z') return (char) (ch & 0x5F);
        return ch;
    }

    private String scheme;
    private String hostAddress;
    private Integer port;
    private StringBuilder path;
    private StringBuffer query;

    public URIBuilder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public URIBuilder hostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
        return this;
    }

    public URIBuilder port(Integer port) {
        this.port = port;
        return this;
    }

    public URIBuilder addPath(String segment) {
        if (path == null) path = new StringBuilder();
        path.append('/').append(encode(P_CHAR, segment));
        return this;
    }

    public URIBuilder addQueryParam(String name, String value) {
        if (query == null) query = new StringBuffer("?");
        else query.append('&');
        query.append(encode(QUERY, name)).append('=').append(encode(QUERY, value));
        return this;
    }

    public String toURI() {
        StringBuilder builder = new StringBuilder(64);

        if (hostAddress != null) {
            if (scheme == null) builder.append("//");
            else builder.append(scheme).append("://");
            builder.append(hostAddress);
            if (port != null) builder.append(':').append(port);
            if (path != null && path.charAt(0) != '/') builder.append('/');
        }

        if (path != null) {
            builder.append(path);
        }

        if (query != null) {
            builder.append(query);
        }

        return builder.toString();
    }
}
