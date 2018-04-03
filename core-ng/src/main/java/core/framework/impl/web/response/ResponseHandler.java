package core.framework.impl.web.response;

import core.framework.api.http.HTTPStatus;
import core.framework.impl.log.ActionLog;
import core.framework.impl.web.HTTPLogger;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import core.framework.impl.web.site.TemplateManager;
import core.framework.util.Encodings;
import core.framework.web.CookieSpec;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;

import java.util.Map;

/**
 * @author neo
 */
public class ResponseHandler {
    private final ResponseHandlerContext context;
    private final HTTPLogger logger;

    public ResponseHandler(ResponseBeanTypeValidator validator, TemplateManager templateManager, HTTPLogger logger) {
        context = new ResponseHandlerContext(validator, templateManager);
        this.logger = logger;
    }

    public void render(ResponseImpl response, HttpServerExchange exchange, ActionLog actionLog) {
        HTTPStatus status = response.status();
        exchange.setStatusCode(status.code);

        putHeaders(response, exchange);
        putCookies(response, exchange);
        response.body.send(exchange.getResponseSender(), context);

        actionLog.context("responseCode", status.code);  // set response code context at last, to avoid error handler to log duplicate action_log_context key on exception
    }

    private void putHeaders(ResponseImpl response, HttpServerExchange exchange) {
        HeaderMap headers = exchange.getResponseHeaders();
        response.headers.forEach(headers::put);
        logger.logResponseHeaders(response.headers);
    }

    private void putCookies(ResponseImpl response, HttpServerExchange exchange) {
        if (response.cookies != null) {
            Map<String, Cookie> cookies = exchange.getResponseCookies();
            response.cookies.forEach((spec, value) -> {
                CookieImpl cookie = cookie(spec, value);
                cookies.put(spec.name, cookie);
            });
            logger.logResponseCookies(cookies);
        }
    }

    CookieImpl cookie(CookieSpec spec, String value) {
        CookieImpl cookie = new CookieImpl(spec.name);
        if (value == null) {
            cookie.setMaxAge(0);
            cookie.setValue("");
        } else {
            if (spec.maxAge != null) cookie.setMaxAge((int) spec.maxAge.getSeconds());
            cookie.setValue(Encodings.uriComponent(value));     // recommended to use URI encoding for cookie value, https://curl.haxx.se/rfc/cookie_spec.html
        }
        cookie.setDomain(spec.domain);
        cookie.setPath(spec.path);
        cookie.setSecure(spec.secure);
        cookie.setHttpOnly(spec.httpOnly);
        return cookie;
    }
}
