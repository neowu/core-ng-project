package core.framework.api.util;

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
    private static final BitSet P_CHAR = new BitSet(128);
    private static final BitSet QUERY_PARAM = new BitSet(128);
    private static final BitSet FRAGMENT = new BitSet(128);
    private static final BitSet GEN_DELIMS = new BitSet(8);

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

        P_CHAR.stream().forEach(FRAGMENT::set);
        FRAGMENT.set('/');
        FRAGMENT.set('?');

        FRAGMENT.stream().forEach(QUERY_PARAM::set);
        QUERY_PARAM.clear('+');   // this is not on rfc3986, but query param can not contains +/=/& (not like query), another wise it will mislead query string parsing
        QUERY_PARAM.clear('=');
        QUERY_PARAM.clear('&');

        GEN_DELIMS.set(':');
        GEN_DELIMS.set('/');
        GEN_DELIMS.set('?');
        GEN_DELIMS.set('#');
        GEN_DELIMS.set('[');
        GEN_DELIMS.set(']');
        GEN_DELIMS.set('@');
    }

    public static boolean isValidURIChar(char ch) {
        return P_CHAR.get(ch) || GEN_DELIMS.get(ch) || ch == '%';
    }

    public static String encodePathSegment(String segment) {
        return encode(P_CHAR, segment);
    }

    static String encodeQueryParam(String param) {
        return encode(QUERY_PARAM, param);
    }

    static String encodeFragment(String fragment) {
        return encode(FRAGMENT, fragment);
    }

    // refer to http://www.ietf.org/rfc/rfc3986.txt
    // refer to org.springframework.web.util.HierarchicalUriComponents#encodeUriComponent
    static String encode(BitSet safeChars, String value) {
        byte[] bytes = Strings.bytes(value);
        ByteBuf buffer = ByteBuf.newBuffer(bytes.length * 2);
        boolean changed = false;
        for (byte b : bytes) {
            if (b >= 0 && safeChars.get(b)) {   // the bytes java returned is signed, but we only need to check ascii (0-127)
                buffer.put(b);
            } else {
                buffer.put((byte) '%');
                char hex1 = toUpper(Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = toUpper(Character.forDigit(b & 0xF, 16));
                buffer.put((byte) hex1);
                buffer.put((byte) hex2);

                changed = true;
            }
        }
        return changed ? buffer.text(StandardCharsets.US_ASCII) : value;    // return original string to help GC
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
        URI parsedURI = URI.create(uri);
        this.hostAddress = parsedURI.getHost();
        this.scheme = parsedURI.getScheme();
        if (parsedURI.getPort() != -1) port = parsedURI.getPort();
        if (parsedURI.getRawPath() != null) path = new StringBuilder(parsedURI.getRawPath());
        if (parsedURI.getRawQuery() != null) query = new StringBuilder(parsedURI.getRawQuery());
        fragment = parsedURI.getRawFragment();
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
        this.fragment = encodeFragment(fragment);
        return this;
    }

    public URIBuilder addSlash() {
        if (path == null) path = new StringBuilder("/");
        else if (path.length() > 0 && path.charAt(path.length() - 1) == '/') throw Exceptions.error("current path already ends with '/', path={}", path);
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
        query.append(encodeQueryParam(name)).append('=').append(encodeQueryParam(value));
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
