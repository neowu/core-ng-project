package core.framework.web;

import core.framework.http.HTTPMethod;

import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public interface Request {
    String requestURL();  // original request url without decoding

    String scheme();

    String hostname();

    String path();      // original path without decoding

    HTTPMethod method();

    Optional<String> header(String name);

    String pathParam(String name);

    Map<String, String> queryParams();

    Map<String, String> formParams();

    Map<String, MultipartFile> files();

    Optional<byte[]> body();

    <T> T bean(Class<T> beanClass);

    String clientIP();  // parsed from x-forwarded-for if present, for logging/rate limiting/etc

    InetAddress remoteIP(); // direct peer ip of the connection, ignores x-forwarded-for, e.g. to verify request is from internal network

    Optional<String> cookie(CookieSpec spec);

    Session session();
}
