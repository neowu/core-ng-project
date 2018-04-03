package core.framework.impl.web;

import core.framework.http.ContentType;
import core.framework.impl.log.param.BytesParam;
import core.framework.impl.web.session.SessionManager;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author neo
 */
public class HTTPLogger {
    private final Logger logger = LoggerFactory.getLogger(HTTPLogger.class);
    private final SessionManager sessionManager;

    public HTTPLogger(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void logResponseHeaders(Map<HttpString, String> headers) {
        headers.forEach((name, value) -> {
            logger.debug("[response:header] {}={}", name, value);
        });
    }

    public void logResponseCookies(Map<String, Cookie> cookies) {
        cookies.forEach((name, cookie) -> {
            String cookieValue = maskCookieValue(name, cookie.getValue());
            logger.debug("[response:cookie] name={}, value={}, domain={}, path={}, secure={}, httpOnly={}, maxAge={}",
                    name, cookieValue, cookie.getDomain(), cookie.getPath(), cookie.isSecure(), cookie.isHttpOnly(), cookie.getMaxAge());
        });
    }

    public void logRequestHeaders(HeaderMap headers, HttpServerExchange exchange) {
        boolean hasCookies = false;
        for (HeaderValues header : headers) {
            if (Headers.COOKIE.equals(header.getHeaderName())) {
                hasCookies = true;
            } else {
                logger.debug("[request:header] {}={}", header.getHeaderName(), header.toArray());
            }
        }
        if (hasCookies) {
            exchange.getRequestCookies().forEach((name, cookie) -> {
                String cookieValue = maskCookieValue(name, cookie.getValue());
                logger.debug("[request:cookie] {}={}", name, cookieValue);
            });
        }
    }

    public void logRequestBody(byte[] body, ContentType contentType) {
        if (contentType != null
                && (ContentType.APPLICATION_JSON.mediaType().equals(contentType.mediaType())
                || ContentType.TEXT_XML.mediaType().equals(contentType.mediaType()))) {
            logger.debug("[request] body={}", new BytesParam(body));
        }
    }

    String maskCookieValue(String name, String value) {
        if (name.equals(sessionManager.sessionId.name)) {
            return "******";
        }
        return value;
    }
}
