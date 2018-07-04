package core.framework.impl.web.request;

import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.impl.validate.ValidationException;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.util.Encodings;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.web.CookieSpec;
import core.framework.web.MultipartFile;
import core.framework.web.Request;
import core.framework.web.Session;
import core.framework.web.exception.BadRequestException;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;

import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class RequestImpl implements Request {
    public final PathParams pathParams = new PathParams();

    final Map<String, String> queryParams = Maps.newHashMap();
    final Map<String, String> formParams = Maps.newHashMap();
    final Map<String, MultipartFile> files = Maps.newHashMap();

    private final HttpServerExchange exchange;
    private final RequestBeanMapper mapper;
    public Session session;
    HTTPMethod method;
    String clientIP;
    String scheme;
    int port;
    String requestURL;
    ContentType contentType;
    byte[] body;

    public RequestImpl(HttpServerExchange exchange, RequestBeanMapper mapper) {
        this.exchange = exchange;
        this.mapper = mapper;
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
    public String hostName() {
        return exchange.getHostName();
    }

    @Override
    public String path() {
        return exchange.getRequestPath();
    }

    @Override
    public String clientIP() {
        return clientIP;
    }

    @Override
    public Optional<String> cookie(CookieSpec spec) {
        Cookie cookie = exchange.getRequestCookies().get(spec.name);
        if (cookie == null) return Optional.empty();
        try {
            return Optional.of(Encodings.decodeURIComponent(cookie.getValue()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage(), BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }

    @Override
    public Session session() {
        if (!"https".equals(scheme)) throw new Error("session must be used under https");
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
    public <T> T pathParam(String name, Class<T> valueClass) {
        String value = pathParams.get(name);
        return URLParamParser.parse(value, valueClass);
    }

    @Override
    public <T> Optional<T> queryParam(String name, Class<T> valueClass) {
        String value = queryParams.get(name);
        if (value == null) return Optional.empty();
        return Optional.of(URLParamParser.parse(value, valueClass));
    }

    @Override
    public Map<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public Optional<String> formParam(String name) {
        return Optional.ofNullable(formParams.get(name));
    }

    @Override
    public Map<String, String> formParams() {
        return formParams;
    }

    @Override
    public Optional<MultipartFile> file(String name) {
        return Optional.ofNullable(files.get(name));
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
                return mapper.fromParams(beanClass, queryParams);
            } else if (method == HTTPMethod.POST || method == HTTPMethod.PUT || method == HTTPMethod.PATCH) {
                if (!formParams.isEmpty()) {
                    return mapper.fromParams(beanClass, formParams);
                } else if (body != null && contentType != null && ContentType.APPLICATION_JSON.mediaType().equals(contentType.mediaType())) {
                    return mapper.fromJSON(beanClass, body);
                }
                throw new BadRequestException("body is missing or unsupported content type, method=" + method + ", contentType=" + contentType);
            } else {
                throw Exceptions.error("not supported method, method={}", method);
            }
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), "VALIDATION_ERROR", e);
        } catch (UncheckedIOException e) {  // for invalid json string
            throw new BadRequestException(e.getMessage(), BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }
}
