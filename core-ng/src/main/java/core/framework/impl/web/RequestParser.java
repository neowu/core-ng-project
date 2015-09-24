package core.framework.impl.web;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Charsets;
import core.framework.api.util.InputStreams;
import core.framework.api.util.Strings;
import core.framework.impl.log.ActionLog;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author neo
 */
public class RequestParser {
    private final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    void parse(RequestImpl request, HttpServerExchange exchange, ActionLog actionLog) throws IOException {
        request.method = HTTPMethod.valueOf(exchange.getRequestMethod().toString());
        actionLog.context("method", request.method());

        HeaderMap headers = exchange.getRequestHeaders();

        String xForwardedFor = headers.getFirst(Headers.X_FORWARDED_FOR);
        String remoteAddress = exchange.getSourceAddress().getAddress().getHostAddress();
        logger.debug("[request] remoteAddress={}", remoteAddress);

        String clientIP = clientIP(remoteAddress, xForwardedFor);
        request.clientIP = clientIP;
        actionLog.context("clientIP", clientIP);

        String xForwardedProto = headers.getFirst(Headers.X_FORWARDED_PROTO);
        String requestScheme = exchange.getRequestScheme();
        logger.debug("[request] requestScheme={}", requestScheme);
        request.scheme = xForwardedProto != null ? xForwardedProto : requestScheme;

        String xForwardedPort = headers.getFirst(Headers.X_FORWARDED_PORT);
        int hostPort = exchange.getHostPort();
        logger.debug("[request] hostPort={}", hostPort);
        request.port = port(hostPort, xForwardedPort);

        actionLog.context("path", request.path());

        String requestURL = requestURL(request, exchange);
        request.requestURL = requestURL;
        logger.debug("[request] requestURL={}", requestURL);
        logger.debug("[request] queryString={}", exchange.getQueryString());

        for (HeaderValues header : headers) {
            logger.debug("[request:header] {}={}", header.getHeaderName(), header.toArray());
        }

        String userAgent = headers.getFirst(Headers.USER_AGENT);
        if (userAgent != null) actionLog.context("userAgent", userAgent);

        if (request.method == HTTPMethod.POST || request.method == HTTPMethod.PUT) {
            request.contentType = headers.getFirst(Headers.CONTENT_TYPE);
            parseBody(request, exchange);
        }
    }

    void parseBody(RequestImpl request, HttpServerExchange exchange) throws IOException {
        if (request.contentType != null && request.contentType.startsWith("application/json")) {
            exchange.startBlocking();
            request.body = new String(readRequestBody(exchange), Charsets.UTF_8);
            logger.debug("[request] body={}", request.body);
        } else if (request.method() == HTTPMethod.POST) {
            FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
            if (formData != null) {
                request.formData = formData;
                for (String name : request.formData) {
                    logger.debug("[request:form] {}={}", name, request.formData.get(name));
                }
            }
        }
    }

    private byte[] readRequestBody(HttpServerExchange exchange) throws IOException {
        int length = (int) exchange.getRequestContentLength();
        try (InputStream stream = exchange.getInputStream()) {
            if (length > 0)
                return InputStreams.readAllWithExpectedSize(stream, length);
            else
                return InputStreams.readAll(stream);
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

    String requestURL(RequestImpl request, HttpServerExchange exchange) {
        if (exchange.isHostIncludedInRequestURI()) {    // GET can use absolute url as request uri, http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html
            return exchange.getRequestURI();
        } else {
            String scheme = request.scheme;
            int port = request.port;

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
}
