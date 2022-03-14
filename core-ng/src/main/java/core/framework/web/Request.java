package core.framework.web;

import core.framework.http.HTTPMethod;

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

    String clientIP();

    Optional<String> cookie(CookieSpec spec);

    Session session();
}
