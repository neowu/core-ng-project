package core.framework.impl.web;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Exceptions;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.web.CookieSpec;
import core.framework.api.web.MultipartFile;
import core.framework.api.web.Request;
import core.framework.api.web.Session;
import core.framework.api.web.exception.BadRequestException;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.form.FormData;
import io.undertow.util.Headers;

import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class RequestImpl implements Request {
    private final HttpServerExchange exchange;
    private final BeanValidator validator;
    HTTPMethod method;
    String clientIP;
    String scheme;
    int port;
    String requestURL;
    String contentType;
    final PathParams pathParams = new PathParams();
    FormData formData;
    String body;
    public Session session;

    RequestImpl(HttpServerExchange exchange, BeanValidator validator) {
        this.exchange = exchange;
        this.validator = validator;
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
        return cookie(spec.name);
    }

    //TODO: inline this, let session manager handle SessionId/SecureSessionId, use CookieSpec
    public Optional<String> cookie(String name) {
        Cookie cookie = exchange.getRequestCookies().get(name);
        if (cookie == null) return Optional.empty();
        return Optional.of(cookie.getValue());
    }

    @Override
    public Session session() {
        if (session == null)
            throw new Error("session store is not configured, please use site() to configure in module");
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
        return pathParams.get(name, valueClass);
    }

    @Override
    public Optional<String> queryParam(String name) {
        Deque<String> values = exchange.getQueryParameters().get(name);
        if (values == null) return Optional.empty();
        return Optional.of(values.getFirst());
    }

    @Override
    public Optional<String> formParam(String name) {
        FormData.FormValue value = formValue(name);
        if (value == null) return Optional.empty();
        return Optional.ofNullable(value.getValue());
    }

    @Override
    public Optional<MultipartFile> file(String name) {
        FormData.FormValue value = formValue(name);
        if (value == null) return Optional.empty();
        if (!value.isFile())
            throw new BadRequestException("form body must be multipart, method=" + method + ", contentType=" + contentType);
        return Optional.of(new MultipartFile(value.getFile(), value.getFileName(), value.getHeaders().getFirst(Headers.CONTENT_TYPE)));
    }

    private FormData.FormValue formValue(String name) {
        if (formData == null)
            throw new BadRequestException("form body is required, method=" + method + ", contentType=" + contentType);

        return formData.getFirst(name);
    }

    @Override
    public <T> T bean(Type instanceType) {
        try {
            T bean = parseBean(instanceType);
            return validator.validate(instanceType, bean);
        } catch (UncheckedIOException e) {
            throw new BadRequestException(e);
        }
    }

    private <T> T parseBean(Type instanceType) {
        if (method == HTTPMethod.GET || method == HTTPMethod.DELETE) {
            Map<String, String> params = Maps.newHashMap();
            exchange.getQueryParameters().forEach((name, values) -> params.put(name, values.element()));
            String formJSON = JSON.toJSON(params);
            return JSON.fromJSON(instanceType, formJSON);
        } else if (method == HTTPMethod.POST || method == HTTPMethod.PUT) {
            if (formData != null) {
                Map<String, String> form = Maps.newHashMap();
                for (String name : formData) {
                    form.put(name, formData.getFirst(name).getValue());
                }
                String formJSON = JSON.toJSON(form);
                return JSON.fromJSON(instanceType, formJSON);
            } else if (body != null && contentType != null && contentType.contains("application/json")) {
                return JSON.fromJSON(instanceType, body);
            }
            throw new BadRequestException("body is required, method=" + method + ", contentType=" + contentType);
        } else {
            throw Exceptions.error("not supported method, method={}", method);
        }
    }
}
