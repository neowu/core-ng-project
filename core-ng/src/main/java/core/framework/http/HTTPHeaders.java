package core.framework.http;

/**
 * @author neo
 */
public final class HTTPHeaders {
    // http header is case insensitive, use lower case value to match jdk and chrome style
    public static final String ACCEPT = "accept";
    public static final String ACCEPT_ENCODING = "accept-encoding"; // TODO: not used anymore if switch to okhttp
    public static final String AUTHORIZATION = "authorization";
    public static final String CACHE_CONTROL = "cache-control";
    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_ENCODING = "content-encoding";
    public static final String LOCATION = "location";
    public static final String USER_AGENT = "user-agent";
}
