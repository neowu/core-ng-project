package core.framework.api.util;

import java.io.ByteArrayOutputStream;
import java.net.URI;
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
    static final BitSet P_CHAR = new BitSet(128);
    static final BitSet QUERY_OR_FRAGMENT = new BitSet(128);
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

        P_CHAR.stream().forEach(QUERY_OR_FRAGMENT::set);
        QUERY_OR_FRAGMENT.set('/');
        QUERY_OR_FRAGMENT.set('?');

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
            if (b >= 0 && safeChars.get(b)) {   // the bytes java returned is signed, but we only need to check ascii (0-127)
                stream.write(b);
            } else {
                stream.write('%');
                char hex1 = toUpper(Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = toUpper(Character.forDigit(b & 0xF, 16));
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
    private StringBuilder query;
    private String fragment;

    public URIBuilder() {
    }

    public URIBuilder(String uri) {
        URI uriValue = URI.create(uri);
        this.hostAddress = uriValue.getHost();
        this.scheme = uriValue.getScheme();
        if (uriValue.getPort() != -1) port = uriValue.getPort();
        if (uriValue.getRawPath() != null) path = new StringBuilder(uriValue.getRawPath());
        if (uriValue.getRawQuery() != null) query = new StringBuilder(uriValue.getRawQuery());
        fragment = uriValue.getRawFragment();
    }

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

    public URIBuilder fragment(String fragment) {
        this.fragment = encode(QUERY_OR_FRAGMENT, fragment);
        return this;
    }

    public URIBuilder addSlash() {
        if (path == null) path = new StringBuilder("/");
        else if (path.length() > 0 && path.charAt(path.length() - 1) == '/') throw Exceptions.error("current path is already ended with '/', path={}", path);
        else path.append('/');
        return this;
    }

    public URIBuilder addPath(String segment) {
        if (path == null) path = new StringBuilder();
        if (path.length() > 0 && path.charAt(path.length() - 1) != '/') path.append('/');
        path.append(encode(P_CHAR, segment));
        return this;
    }

    public URIBuilder addQueryParam(String name, String value) {
        if (query == null) query = new StringBuilder();
        else query.append('&');
        query.append(encode(QUERY_OR_FRAGMENT, name)).append('=').append(encode(QUERY_OR_FRAGMENT, value));
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
            builder.append('?').append(query);
        }

        if (fragment != null) {
            builder.append('#').append(fragment);
        }

        return builder.toString();
    }
}
