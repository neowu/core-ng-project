package core.framework.internal.web.request;

import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.internal.validate.ValidationException;
import core.framework.internal.web.bean.RequestBeanReader;
import core.framework.util.Maps;
import core.framework.web.CookieSpec;
import core.framework.web.MultipartFile;
import core.framework.web.Request;
import core.framework.web.Session;
import core.framework.web.exception.BadRequestException;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class RequestImpl implements Request {
    public final PathParams pathParams = new PathParams();

    final Map<String, String> queryParams = Maps.newHashMap();
    final Map<String, String> formParams = Maps.newHashMap();
    final Map<String, MultipartFile> files = Maps.newHashMap();

    private final HttpServerExchange exchange;
    private final RequestBeanReader reader;
    public Session session;

    HTTPMethod method;
    String clientIP;
    String scheme;
    String hostname;
    int port;
    String path;
    String requestURL;
    ContentType contentType;
    byte[] body;
    Map<String, String> cookies;

    public RequestImpl(HttpServerExchange exchange, RequestBeanReader reader) {
        this.exchange = exchange;
        this.reader = reader;
    }

    @Override
    public String requestURL() {
        return requestURL;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    @Override
    public String hostname() {
        return hostname;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String clientIP() {
        return clientIP;
    }

    @Override
    public Optional<String> cookie(CookieSpec spec) {
        if (cookies == null) return Optional.empty();
        return Optional.ofNullable(cookies.get(spec.name));
    }

    @Override
    public Session session() {
        if (!"https".equals(scheme)) throw new Error("session must be used with https");
        if (session == null) throw new Error("site().session() must be configured");
        return session;
    }

    @Override
    public HTTPMethod method() {
        return method;
    }

    @Override
    public Optional<String> header(String name) {
        return Optional.ofNullable(exchange.getRequestHeaders().getFirst(name));
    }

    @Override
    public String pathParam(String name) {
        return pathParams.get(name);
    }

    @Override
    public Map<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public Map<String, String> formParams() {
        return formParams;
    }

    @Override
    public Map<String, MultipartFile> files() {
        return files;
    }

    @Override
    public Optional<byte[]> body() {
        return Optional.ofNullable(body);
    }

    @Override
    public <T> T bean(Class<T> beanClass) {
        try {
            if (method == HTTPMethod.GET || method == HTTPMethod.DELETE) {
                return reader.fromParams(beanClass, queryParams);
            } else if (method == HTTPMethod.POST || method == HTTPMethod.PUT || method == HTTPMethod.PATCH) {
                if (!formParams.isEmpty()) {
                    return reader.fromParams(beanClass, formParams);
                } else if (body != null && contentType != null && ContentType.APPLICATION_JSON.mediaType.equals(contentType.mediaType)) {
                    return reader.fromJSON(beanClass, body);
                }
                throw new BadRequestException(format("body is missing or unsupported content type, method={}, contentType={}", method, contentType), "INVALID_HTTP_REQUEST");
            } else {
                throw new Error("not supported method, method=" + method);
            }
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e.errorCode(), e);
        } catch (IOException e) {  // for invalid json string
            // for security concern, to hide original error message, jackson may return detailed info, e.g. possible allowed values for enum
            // detailed info can still be found in trace log or exception stack trace
            throw new BadRequestException("failed to deserialize request, beanClass=" + beanClass.getCanonicalName(), "INVALID_HTTP_REQUEST", e);
        }
    }
}
