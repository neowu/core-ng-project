package core.framework.impl.web;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Exceptions;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.web.CookieSpec;
import core.framework.api.web.Request;
import core.framework.api.web.Session;
import core.framework.api.web.exception.BadRequestException;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.form.FormData;
import io.undertow.util.HeaderMap;
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

    private final RequestProtocol protocol;
    final RemoteAddress remoteAddress;
    final HTTPMethod method;
    final String contentType;
    final PathParams pathParams = new PathParams();
    FormData formData;
    String body;
    public Session session;

    RequestImpl(HttpServerExchange exchange, BeanValidator validator) {
        this.exchange = exchange;
        this.validator = validator;

        method = HTTPMethod.valueOf(exchange.getRequestMethod().toString());
        HeaderMap headers = exchange.getRequestHeaders();

        String xForwardedFor = headers.getFirst(Headers.X_FORWARDED_FOR);
        remoteAddress = new RemoteAddress(exchange.getSourceAddress().getAddress().getHostAddress(), xForwardedFor);

        String xForwardedProto = headers.getFirst(Headers.X_FORWARDED_PROTO);
        String xForwardedPort = headers.getFirst(Headers.X_FORWARDED_PORT);
        protocol = new RequestProtocol(exchange.getRequestScheme(), xForwardedProto, exchange.getHostPort(), xForwardedPort);

        if (method == HTTPMethod.POST || method == HTTPMethod.PUT) {
            contentType = headers.getFirst(Headers.CONTENT_TYPE);
        } else {
            contentType = null;
        }
    }

    @Override
    public String requestURL() {
        if (exchange.isHostIncludedInRequestURI()) {    // GET can use absolute url as request uri, http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html
            return exchange.getRequestURI();
        } else {
            String scheme = protocol.scheme();
            int port = protocol.port();

            StringBuilder builder = new StringBuilder(scheme)
                .append("://")
                .append(exchange.getHostName());

            if (!(("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443))) {
                builder.append(':').append(port);
            }

            builder.append(exchange.getRequestURI());
            return builder.toString();
        }
    }

    @Override
    public String scheme() {
        return protocol.scheme();
    }

    @Override
    public String host() {
        return exchange.getHostName();
    }

    @Override
    public String path() {
        return exchange.getRequestPath();
    }

    @Override
    public String clientIP() {
        return remoteAddress.clientIP();
    }

    @Override
    public Optional<String> cookie(CookieSpec spec) {
        return cookie(spec.name);
    }

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
        if (formData == null)
            throw new BadRequestException("form body is required, method=" + method + ", contentType=" + contentType);

        return Optional.ofNullable(formData.getFirst(name).getValue());
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
