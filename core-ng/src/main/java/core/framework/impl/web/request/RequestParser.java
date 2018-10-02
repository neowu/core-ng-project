package core.framework.impl.web.request;

import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.impl.http.BodyLogParam;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.filter.FieldLogParam;
import core.framework.util.Files;
import core.framework.util.Strings;
import core.framework.web.MultipartFile;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.MethodNotAllowedException;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.EnumSet;
import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class RequestParser {
    private static final int MAX_URL_LENGTH = 1000;
    public final ClientIPParser clientIPParser = new ClientIPParser();
    private final Logger logger = LoggerFactory.getLogger(RequestParser.class);
    private final EnumSet<HTTPMethod> withBodyMethods = EnumSet.of(HTTPMethod.POST, HTTPMethod.PUT, HTTPMethod.PATCH);

    public void parse(RequestImpl request, HttpServerExchange exchange, ActionLog actionLog) throws Throwable {
        HeaderMap headers = exchange.getRequestHeaders();

        request.scheme = scheme(exchange.getRequestScheme(), headers.getFirst(Headers.X_FORWARDED_PROTO));
        int requestPort = requestPort(headers.getFirst(Headers.HOST), request.scheme, exchange);
        request.port = port(requestPort, headers.getFirst(Headers.X_FORWARDED_PORT));

        String remoteAddress = exchange.getSourceAddress().getAddress().getHostAddress();
        logger.debug("[request] remoteAddress={}", remoteAddress);
        request.clientIP = clientIPParser.parse(remoteAddress, headers.getFirst(Headers.X_FORWARDED_FOR));
        actionLog.context("clientIP", request.clientIP);

        String method = exchange.getRequestMethod().toString();
        actionLog.context("method", method);

        request.requestURL = requestURL(request, exchange);
        actionLog.context("requestURL", request.requestURL);
        request.path = path(exchange);

        logHeaders(headers, exchange);

        String userAgent = headers.getFirst(Headers.USER_AGENT);
        if (userAgent != null) actionLog.context("userAgent", userAgent);

        request.method = httpMethod(method);    // parse method after logging header/etc, to gather more info in case we see unsupported method passed from internet

        parseQueryParams(request, exchange.getQueryParameters());

        if (withBodyMethods.contains(request.method)) {
            String contentType = headers.getFirst(Headers.CONTENT_TYPE);
            request.contentType = contentType == null ? null : ContentType.parse(contentType);
            parseBody(request, exchange);
        }
    }

    String scheme(String requestScheme, String xForwardedProto) {       // xForwardedProto is single value, refer to https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto
        logger.debug("[request] requestScheme={}", requestScheme);
        return xForwardedProto != null ? xForwardedProto : requestScheme;
    }

    private void logHeaders(HeaderMap headers, HttpServerExchange exchange) {
        boolean hasCookies = false;
        for (HeaderValues header : headers) {
            HttpString name = header.getHeaderName();
            if (Headers.COOKIE.equals(name)) {
                hasCookies = true;
            } else {
                logger.debug("[request:header] {}={}", name, new HeaderLogParam(name, header));
            }
        }
        if (hasCookies) {
            for (Map.Entry<String, Cookie> entry : exchange.getRequestCookies().entrySet()) {
                String name = entry.getKey();
                Cookie cookie = entry.getValue();
                logger.debug("[request:cookie] {}={}", name, new FieldLogParam(name, cookie.getValue()));
            }
        }
    }

    void parseQueryParams(RequestImpl request, Map<String, Deque<String>> params) {
        for (Map.Entry<String, Deque<String>> entry : params.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue().getFirst();
            logger.debug("[request:query] {}={}", name, value);
            request.queryParams.put(name, value);
        }
    }

    HTTPMethod httpMethod(String method) {
        try {
            return HTTPMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            throw new MethodNotAllowedException("method is not allowed, method=" + method, e);
        }
    }

    void parseBody(RequestImpl request, HttpServerExchange exchange) throws Throwable {
        var body = exchange.getAttachment(RequestBodyReader.REQUEST_BODY);
        if (body != null) {
            request.body = body.body();
            logger.debug("[request] body={}", BodyLogParam.param(request.body, request.contentType));
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
                if (!Strings.isBlank(value.getFileName())) {    // browser passes blank file name if not choose file in form
                    logger.debug("[request:file] {}={}, size={}", name, value.getFileName(), Files.size(value.getPath()));
                    request.files.put(name, new MultipartFile(value.getPath(), value.getFileName(), value.getHeaders().getFirst(Headers.CONTENT_TYPE)));
                }
            } else {
                logger.debug("[request:form] {}={}", name, new FieldLogParam(name, value.getValue()));
                request.formParams.put(name, value.getValue());
            }
        }
    }

    int port(int requestPort, String xForwardedPort) {
        if (xForwardedPort != null) {
            int index = xForwardedPort.indexOf(',');
            if (index > 0)
                return Integer.parseInt(xForwardedPort.substring(0, index));
            else
                return Integer.parseInt(xForwardedPort);
        }
        return requestPort;
    }

    // due to google cloud LB does not forward x-forwarded-port, here is to use x-forwarded-proto to determine port if any
    int requestPort(String host, String scheme, HttpServerExchange exchange) {    // refer to io.undertow.server.HttpServerExchange.getHostPort(), use x-forwarded-proto as request scheme
        if (host != null) {     // HOST header is must for http/1.1, refer to https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
            int colonIndex;
            if (Strings.startsWith(host, '[')) { //for ipv6 addresses we make sure we take out the first part, which can have multiple occurrences of :
                colonIndex = host.indexOf(':', host.indexOf(']'));
            } else {
                colonIndex = host.indexOf(':');
            }
            if (colonIndex > 0 && colonIndex + 1 < host.length()) { // parse port only if ':' is in middle of string
                return Integer.parseInt(host.substring(colonIndex + 1));
            }
            // return default port according to scheme
            if ("https".equals(scheme)) return 443;
            if ("http".equals(scheme)) return 80;
        }
        return exchange.getDestinationAddress().getPort();
    }

    String requestURL(RequestImpl request, HttpServerExchange exchange) {
        var builder = new StringBuilder(128);

        if (exchange.isHostIncludedInRequestURI()) {    // GET can use absolute url as request uri, http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html, when client sends request via forward proxy
            builder.append(exchange.getRequestURI());
        } else {
            String scheme = request.scheme;
            int port = request.port;

            builder.append(scheme)
                   .append("://")
                   .append(exchange.getHostName());

            if (!(port == 80 && "http".equals(scheme)) && !(port == 443 && "https".equals(scheme))) {
                builder.append(':').append(port);
            }

            builder.append(exchange.getRequestURI());
        }

        String queryString = exchange.getQueryString();
        if (!queryString.isEmpty()) builder.append('?').append(queryString);

        String requestURL = builder.toString();
        if (requestURL.length() > MAX_URL_LENGTH) throw new BadRequestException(format("requestURL is too long, requestURL={}...(truncated)", requestURL.substring(0, 50)), "INVALID_HTTP_REQUEST");
        return requestURL;
    }

    // not decoded path, in case there is '/' in decoded value to interfere path pattern matching
    String path(HttpServerExchange exchange) {
        String path = exchange.getRequestURI();
        if (!exchange.isHostIncludedInRequestURI()) return path;

        int index = path.indexOf("//");
        if (index != -1) {
            index = path.indexOf('/', index + 2);
            if (index != -1) {
                return path.substring(index);
            }
        }

        return path;
    }
}
