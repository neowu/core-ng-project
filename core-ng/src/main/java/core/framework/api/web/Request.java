package core.framework.api.web;

import core.framework.api.http.HTTPMethod;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public interface Request {
    String requestURL();  //original request url without decoding

    String scheme();

    String hostName();

    String path();  //requestPath decoded by server

    HTTPMethod method();

    Optional<String> header(String name);

    <T> T pathParam(String name, Class<T> valueClass);

    <T> Optional<T> queryParam(String name, Class<T> valueClass);

    Map<String, String> queryParams();

    Optional<String> formParam(String name);

    Map<String, String> formParams();

    Optional<MultipartFile> file(String name);

    Map<String, MultipartFile> files();

    Optional<byte[]> body();

    <T> T bean(Type instanceType);

    String clientIP();

    Optional<String> cookie(CookieSpec spec);

    Session session();

    default String pathParam(String name) {
        return pathParam(name, String.class);
    }

    default Optional<String> queryParam(String name) {
        return queryParam(name, String.class);
    }
}
