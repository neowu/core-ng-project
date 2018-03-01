package core.framework.impl.web.request;

import core.framework.http.ContentType;
import core.framework.http.HTTPMethod;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogParam;
import core.framework.util.Files;
import core.framework.util.Strings;
import core.framework.web.MultipartFile;
import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.MethodNotAllowedException;
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
    private static final int MAX_URL_LENGTH = 1000;
    public final ClientIPParser clientIPParser = new ClientIPParser();

    private final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    public void parse(RequestImpl request, HttpServerExchange exchange, ActionLog actionLog) throws Throwable {
        HeaderMap headers = exchange.getRequestHeaders();
        for (HeaderValues header : headers) {
            logger.debug("[request:header] {}={}", header.getHeaderName(), header.toArray());
        }

        String remoteAddress = exchange.getSourceAddress().getAddress().getHostAddress();
        logger.debug("[request] remoteAddress={}", remoteAddress);

        request.clientIP = clientIPParser.parse(remoteAddress, headers.getFirst(Headers.X_FORWARDED_FOR));
        actionLog.context("clientIP", request.clientIP);

        String requestScheme = exchange.getRequestScheme();
        logger.debug("[request] requestScheme={}", requestScheme);
        String xForwardedProto = headers.getFirst(Headers.X_FORWARDED_PROTO);
        request.scheme = xForwardedProto != null ? xForwardedProto : requestScheme;

        int requestPort = requestPort(exchange.getRequestHeaders().getFirst(Headers.HOST), request.scheme, exchange);
        request.port = port(requestPort, headers.getFirst(Headers.X_FORWARDED_PORT));

        request.requestURL = requestURL(request, exchange);
        actionLog.context("requestURL", request.requestURL);

        logger.debug("[request] path={}", request.path());

        String userAgent = headers.getFirst(Headers.USER_AGENT);
        if (userAgent != null) actionLog.context("userAgent", userAgent);

        request.method = httpMethod(exchange.getRequestMethod().toString());
        actionLog.context("method", request.method());      // for public site, there will be sniff requests from various sources. as it throws exception with unsupported method, so to log method at last, we can see details (headers) in trace for those illegal requests

        parseQueryParams(request, exchange.getQueryParameters());

        if (request.method == HTTPMethod.POST || request.method == HTTPMethod.PUT || request.method == HTTPMethod.PATCH) {
            String contentType = headers.getFirst(Headers.CONTENT_TYPE);
            request.contentType = contentType == null ? null : ContentType.parse(contentType);
            parseBody(request, exchange);
        }
    }

    void parseQueryParams(RequestImpl request, Map<String, Deque<String>> params) {
        for (Map.Entry<String, Deque<String>> entry : params.entrySet()) {
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

    void parseBody(RequestImpl request, HttpServerExchange exchange) throws Throwable {
        RequestBodyReader.RequestBody body = exchange.getAttachment(RequestBodyReader.REQUEST_BODY);
        if (body != null) {
            request.body = body.body();
            if (request.contentType != null
                    && (ContentType.APPLICATION_JSON.mediaType().equals(request.contentType.mediaType())
                    || ContentType.TEXT_XML.mediaType().equals(request.contentType.mediaType()))) {
                logger.debug("[request] body={}", LogParam.of(request.body));
            }
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
        if (host != null) {
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
        StringBuilder builder = new StringBuilder();

        if (exchange.isHostIncludedInRequestURI()) {    // GET can use absolute url as request uri, http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html
            builder.append(exchange.getRequestURI());
        } else {
            String scheme = request.scheme;
            int port = request.port;

            builder.append(scheme)
                   .append("://")
                   .append(exchange.getHostName());

            if (!("http".equals(scheme) && port == 80) && !("https".equals(scheme) && port == 443)) {
                builder.append(':').append(port);
            }

            builder.append(exchange.getRequestURI());
        }

        String queryString = exchange.getQueryString();
        if (!Strings.isEmpty(queryString)) builder.append('?').append(queryString);

        String requestURL = builder.toString();
        if (requestURL.length() > MAX_URL_LENGTH) throw new BadRequestException(Strings.format("requestURL is too long, requestURL={}...(truncated)", requestURL.substring(0, MAX_URL_LENGTH / 8)));
        return requestURL;
    }
}
