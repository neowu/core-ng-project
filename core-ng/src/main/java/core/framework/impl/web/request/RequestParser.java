package core.framework.impl.web.request;

import core.framework.api.http.ContentType;
import core.framework.api.http.HTTPMethod;
import core.framework.api.log.Markers;
import core.framework.api.util.Files;
import core.framework.api.util.Strings;
import core.framework.api.web.MultipartFile;
import core.framework.api.web.exception.MethodNotAllowedException;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogParam;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Deque;
import java.util.Map;

/**
 * @author neo
 */
public final class RequestParser {
    private final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    public void parse(RequestImpl request, HttpServerExchange exchange, ActionLog actionLog) throws Throwable {
        HeaderMap headers = exchange.getRequestHeaders();

        String xForwardedFor = headers.getFirst(Headers.X_FORWARDED_FOR);
        String remoteAddress = exchange.getSourceAddress().getAddress().getHostAddress();
        logger.debug("[request] remoteAddress={}", remoteAddress);

        request.clientIP = clientIP(remoteAddress, xForwardedFor);
        actionLog.context("clientIP", request.clientIP);

        String xForwardedProto = headers.getFirst(Headers.X_FORWARDED_PROTO);
        String requestScheme = exchange.getRequestScheme();
        logger.debug("[request] requestScheme={}", requestScheme);
        request.scheme = xForwardedProto != null ? xForwardedProto : requestScheme;

        String xForwardedPort = headers.getFirst(Headers.X_FORWARDED_PORT);
        int hostPort = exchange.getHostPort();
        logger.debug("[request] hostPort={}", hostPort);
        request.port = port(hostPort, xForwardedPort);

        request.requestURL = requestURL(request, exchange);
        actionLog.context("requestURL", request.requestURL);

        for (HeaderValues header : headers) {
            logger.debug("[request:header] {}={}", header.getHeaderName(), header.toArray());
        }

        logger.debug("[request] path={}", request.path());

        String userAgent = headers.getFirst(Headers.USER_AGENT);
        if (userAgent != null) actionLog.context("userAgent", userAgent);

        request.method = httpMethod(exchange.getRequestMethod().toString());
        actionLog.context("method", request.method());

        parseQueryParams(request, exchange);

        if (request.method == HTTPMethod.POST || request.method == HTTPMethod.PUT) {
            String contentType = headers.getFirst(Headers.CONTENT_TYPE);
            request.contentType = contentType == null ? null : ContentType.parse(contentType);
            parseBody(request, exchange);
        }
    }

    private void parseQueryParams(RequestImpl request, HttpServerExchange exchange) {
        for (Map.Entry<String, Deque<String>> entry : exchange.getQueryParameters().entrySet()) {
            String name = decodeQueryParam(entry.getKey());
            String value = decodeQueryParam(entry.getValue().getFirst());
            logger.debug("[request:query] {}={}", name, value);
            request.queryParams.put(name, value);
        }
    }

    private String decodeQueryParam(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    HTTPMethod httpMethod(String method) {
        try {
            return HTTPMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            throw new MethodNotAllowedException("method is not allowed, method=" + method, e);
        }
    }

    private void parseBody(RequestImpl request, HttpServerExchange exchange) throws Throwable {
        RequestBodyReader.RequestBody body = exchange.getAttachment(RequestBodyReader.REQUEST_BODY);
        if (body != null) {
            if (request.contentType == null) return;    // pass if post empty body without content type

            if (ContentType.APPLICATION_JSON.mediaType().equals(request.contentType.mediaType())) {
                request.body = body.body();
                logger.debug("[request] body={}", LogParam.of(request.body));
            } else {
                logger.warn(Markers.errorCode("UNSUPPORTED_CONTENT_TYPE"), "unsupported content type, contentType={}", request.contentType);
            }
            exchange.removeAttachment(RequestBodyReader.REQUEST_BODY);
        } else {
            parseForm(request, exchange);
        }
    }

    private void parseForm(RequestImpl request, HttpServerExchange exchange) {
        FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
        if (formData == null) return;

        for (String name : formData) {
            FormData.FormValue value = formData.getFirst(name);
            if (value.isFile()) {
                if (!Strings.isEmpty(value.getFileName())) {    // browser passes empty file name if not choose file in form
                    logger.debug("[request:file] {}={}, size={}", name, value.getFileName(), Files.size(value.getPath()));
                    request.files.put(name, new MultipartFile(value.getPath(), value.getFileName(), value.getHeaders().getFirst(Headers.CONTENT_TYPE)));
                }
            } else {
                logger.debug("[request:form] {}={}", name, value.getValue());
                request.formParams.put(name, value.getValue());
            }
        }
    }

    String clientIP(String remoteAddress, String xForwardedFor) {
        if (Strings.isEmpty(xForwardedFor))
            return remoteAddress;
        int index = xForwardedFor.indexOf(',');
        if (index > 0)
            return xForwardedFor.substring(0, index);
        return xForwardedFor;
    }

    int port(int hostPort, String xForwardedPort) {
        if (xForwardedPort != null) {
            int index = xForwardedPort.indexOf(',');
            if (index > 0)
                return Integer.parseInt(xForwardedPort.substring(0, index));
            else
                return Integer.parseInt(xForwardedPort);
        }
        return hostPort;
    }

    private String requestURL(RequestImpl request, HttpServerExchange exchange) {
        StringBuilder builder = new StringBuilder();

        if (exchange.isHostIncludedInRequestURI()) {    // GET can use absolute url as request uri, http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html
            builder.append(exchange.getRequestURI());
        } else {
            String scheme = request.scheme;
            int port = request.port;

            builder.append(scheme)
                   .append("://")
                   .append(exchange.getHostName());

            if (!(("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443))) {
                builder.append(':').append(port);
            }

            builder.append(exchange.getRequestURI());
        }

        String queryString = exchange.getQueryString();
        if (!Strings.isEmpty(queryString)) builder.append('?').append(queryString);

        return builder.toString();
    }
}
