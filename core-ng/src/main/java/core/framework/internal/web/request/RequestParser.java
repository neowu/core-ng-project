package core.framework.internal.web.request;

import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.internal.http.BodyLogParam;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.filter.FieldLogParam;
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

import java.io.IOException;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static core.framework.log.Markers.errorCode;
import static core.framework.util.Encodings.decodeURIComponent;
import static core.framework.util.Strings.format;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class RequestParser {
    static final int MAX_URL_LENGTH = 2000;
    public final ClientIPParser clientIPParser = new ClientIPParser();
    private final Logger logger = LoggerFactory.getLogger(RequestParser.class);
    private final Set<HTTPMethod> withBodyMethods = EnumSet.of(HTTPMethod.POST, HTTPMethod.PUT, HTTPMethod.PATCH);
    public boolean logSiteHeaders;

    public void parse(RequestImpl request, HttpServerExchange exchange, ActionLog actionLog) throws Throwable {
        HeaderMap headers = exchange.getRequestHeaders();

        request.scheme = scheme(exchange.getRequestScheme(), headers.getFirst(Headers.X_FORWARDED_PROTO));
        request.hostname = hostname(exchange.getHostName(), headers.getFirst(Headers.X_FORWARDED_HOST));
        int requestPort = requestPort(headers.getFirst(Headers.HOST), request.scheme, exchange);
        request.port = port(requestPort, headers.getFirst(Headers.X_FORWARDED_PORT));

        String method = exchange.getRequestMethod().toString();
        actionLog.context.put("method", List.of(method));

        String path = exchange.getRequestPath();
        request.path = path.isEmpty() ? "/" : path; // according to https://www.rfc-editor.org/rfc/rfc2616#section-5.1.2, empty path must be normalized as '/'

        request.requestURL = requestURL(request, exchange);
        putContext(actionLog, "request_url", request.requestURL);   // record request url even if attacker passes long string
        logger.debug("[request] method={}, requestURL={}", method, request.requestURL);

        logHeaders(headers);
        parseClientIP(request, exchange, actionLog, headers.getFirst(Headers.X_FORWARDED_FOR)); // parse client ip after logging header, as ip in x-forwarded-for may be invalid
        parseCookies(request, exchange);

        // parse method, validate url length after logging header/etc, to gather more info in case we see arbitrary attacking request from internet
        request.method = httpMethod(method);
        if (request.requestURL.length() > MAX_URL_LENGTH) throw new BadRequestException(format("requestURL is too long, requestURL={}...(truncated)", request.requestURL.substring(0, 50)), "INVALID_HTTP_REQUEST");

        logSiteHeaders(headers, actionLog);
        parseQueryParams(request, exchange.getQueryParameters());

        if (withBodyMethods.contains(request.method)) {
            String contentType = headers.getFirst(Headers.CONTENT_TYPE);
            request.contentType = contentType == null ? null : ContentType.parse(contentType);
            parseBody(request, exchange);
            actionLog.stats.put("request_body_length", (double) exchange.getRequestContentLength());
        }
    }

    String hostname(String hostname, String xForwardedHost) {
        if (Strings.isBlank(xForwardedHost)) return hostname;
        return xForwardedHost;
    }

    void parseCookies(RequestImpl request, HttpServerExchange exchange) {
        HeaderValues cookieHeaders = exchange.getRequestHeaders().get(Headers.COOKIE);
        if (cookieHeaders != null) {
            try {
                request.cookies = decodeCookies(exchange.requestCookies());
            } catch (IllegalArgumentException | IllegalStateException e) {
                logger.debug("[request:header] {}={}", Headers.COOKIE, new HeaderLogParam(Headers.COOKIE, cookieHeaders));
                // entire cookie will be failed to parse if there is poison cookie value, undertow doesn't provide API to parse cookie individually
                // since it's rare case, here is to use simplified solution to block entire request, rather than hide which may cause potential issues, e.g. never get SESSION_ID to login
                // so this force client to clear all cookies and refresh
                // refer to io.undertow.UndertowMessages.couldNotParseCookie
                // refer to io.undertow.UndertowMessages.tooManyCookies
                throw new BadRequestException("invalid cookie", "INVALID_COOKIE", e);
            }
        }
    }

    private void parseClientIP(RequestImpl request, HttpServerExchange exchange, ActionLog actionLog, String xForwardedFor) {
        String remoteAddress = exchange.getSourceAddress().getAddress().getHostAddress();
        request.clientIP = clientIPParser.parse(remoteAddress, xForwardedFor);
        actionLog.context.put("client_ip", List.of(request.clientIP));
        if (clientIPParser.hasMoreThanMaxForwardedIPs(xForwardedFor)) {
            actionLog.context.put("x_forwarded_for", List.of(xForwardedFor));
        }
        logger.debug("[request] remoteAddress={}, clientIP={}", remoteAddress, request.clientIP);
    }

    String scheme(String requestScheme, String xForwardedProto) {       // xForwardedProto is single value, refer to https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-Proto
        return xForwardedProto != null ? xForwardedProto : requestScheme;
    }

    private void logHeaders(HeaderMap headers) {
        for (HeaderValues header : headers) {
            HttpString name = header.getHeaderName();
            if (!Headers.COOKIE.equals(name)) {
                logger.debug("[request:header] {}={}", name, new HeaderLogParam(name, header));
            }
        }
    }

    void logSiteHeaders(HeaderMap headers, ActionLog actionLog) {
        if (logSiteHeaders) {
            String userAgent = headers.getFirst(Headers.USER_AGENT);
            if (userAgent != null) putContext(actionLog, "user_agent", userAgent);
            String referer = headers.getFirst(Headers.REFERER);
            if (referer != null) putContext(actionLog, "referer", referer);
        }
    }

    Map<String, String> decodeCookies(Iterable<Cookie> cookies) {
        Map<String, String> cookieValues = new HashMap<>();
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            String value = cookie.getValue();
            try {
                String cookieName = decodeURIComponent(name);
                String cookieValue = decodeURIComponent(value);
                logger.debug("[request:cookie] {}={}", cookieName, new FieldLogParam(cookieName, cookieValue));
                cookieValues.put(cookieName, cookieValue);
            } catch (IllegalArgumentException e) {
                // cookies is persistent in browser, here is to ignore, in case user may not be able to access website with legacy cookie
                logger.warn(errorCode("INVALID_COOKIE"), "ignore invalid encoded cookie, name={}, value={}", name, value, e);
            }
        }
        return cookieValues;
    }

    void parseQueryParams(RequestImpl request, Map<String, Deque<String>> params) {
        for (Map.Entry<String, Deque<String>> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getFirst(); // params deque won't be empty so don't expect exception here
            try {
                String paramName = decode(key, UTF_8);
                String paramValue = decode(value, UTF_8);
                logger.debug("[request:query] {}={}", paramName, paramValue);
                request.queryParams.put(paramName, paramValue);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(format("failed to parse query param, name={}, value={}", key, value), "INVALID_HTTP_REQUEST", e);
            }
        }
    }

    HTTPMethod httpMethod(String method) {
        try {
            return HTTPMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            throw new MethodNotAllowedException("method not allowed, method=" + method, e);
        }
    }

    void parseBody(RequestImpl request, HttpServerExchange exchange) throws Throwable {
        RequestBodyReader.RequestBody body = exchange.getAttachment(RequestBodyReader.REQUEST_BODY);
        if (body != null) {
            request.body = body.body();
            logger.debug("[request] body={}", BodyLogParam.of(request.body, request.contentType));
        } else {
            parseForm(request, exchange);
        }
    }

    private void parseForm(RequestImpl request, HttpServerExchange exchange) throws IOException {
        FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
        if (formData == null) return;

        for (String name : formData) {
            FormData.FormValue value = formData.getFirst(name);
            if (value.isFileItem()) {
                String fileName = value.getFileName();
                if (!Strings.isBlank(fileName)) {    // browser passes blank file name if not choose file in form
                    FormData.FileItem item = value.getFileItem();
                    logger.debug("[request:file] {}={}, size={}", name, fileName, item.getFileSize());
                    request.files.put(name, new MultipartFile(item.getFile(), fileName, value.getHeaders().getFirst(Headers.CONTENT_TYPE)));
                }
            } else {
                logger.debug("[request:form] {}={}", name, new FieldLogParam(name, value.getValue()));
                request.formParams.put(name, value.getValue());
            }
        }
    }

    int port(int requestPort, String xForwardedPort) {
        if (xForwardedPort != null) {
            try {
                int index = xForwardedPort.indexOf(',');
                if (index > 0) {
                    return Integer.parseInt(xForwardedPort.substring(0, index));
                } else {
                    return Integer.parseInt(xForwardedPort);
                }
            } catch (NumberFormatException e) {
                // to protect X-Forwarded-Port spoofing
                throw new BadRequestException("invalid port", "INVALID_HTTP_REQUEST", e);
            }
        }
        return requestPort;
    }

    // due to google cloud LB does not forward x-forwarded-port, here is to use x-forwarded-proto to determine port if any
    int requestPort(String host, String scheme, HttpServerExchange exchange) {    // refer to io.undertow.server.HttpServerExchange.getHostPort(), use x-forwarded-proto as request scheme
        if (host != null) {     // HOST header is must for http/1.1, refer to https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
            int colonIndex;
            if (Strings.startsWith(host, '[')) { //for ipv6 addresses make sure to take out the first part, which can have multiple occurrences of ':', e.g. Host: [::1]:5001
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

        // GET can use absolute url as request uri, http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html, when client sends request via forward proxy
        if (!exchange.isHostIncludedInRequestURI()) {
            String scheme = request.scheme;
            int port = request.port;

            builder.append(scheme)
                .append("://")
                .append(request.hostname);

            if (!(port == 80 && "http".equals(scheme)) && !(port == 443 && "https".equals(scheme))) {
                builder.append(':').append(port);
            }
        }

        builder.append(exchange.getRequestURI());

        String queryString = exchange.getQueryString();
        if (!queryString.isEmpty()) builder.append('?').append(queryString);

        return builder.toString();
    }

    // directly put to actionLog context with truncation, to handle attacking request gracefully
    // to unify request logging, and to be slightly efficient
    void putContext(ActionLog actionLog, String key, String value) {
        String contextValue = value.length() <= ActionLog.MAX_CONTEXT_VALUE_LENGTH ? value : value.substring(0, ActionLog.MAX_CONTEXT_VALUE_LENGTH);
        actionLog.context.put(key, List.of(contextValue));
    }
}
