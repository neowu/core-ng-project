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

    String hostName();

    String path();

    HTTPMethod method();

    Optional<String> header(String name);

    <T> T pathParam(String name, Class<T> valueClass);

    default String pathParam(String name) {
        return pathParam(name, String.class);
    }

    <T> Optional<T> queryParam(String name, Class<T> valueClass);

    default Optional<String> queryParam(String name) {
        return queryParam(name, String.class);
    }

    Map<String, String> queryParams();

    Optional<String> formParam(String name);

    Map<String, String> formParams();

    Optional<MultipartFile> file(String name);

    Map<String, MultipartFile> files();

    Optional<byte[]> body();

    <T> T bean(Class<T> beanClass);

    String clientIP();

    Optional<String> cookie(CookieSpec spec);

    Session session();
}
