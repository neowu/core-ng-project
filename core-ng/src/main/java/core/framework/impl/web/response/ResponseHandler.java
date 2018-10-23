package core.framework.impl.web.response;

import core.framework.api.http.HTTPStatus;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.filter.FieldLogParam;
import core.framework.impl.web.ShutdownHandler;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.impl.web.site.TemplateManager;
import core.framework.util.Encodings;
import core.framework.web.CookieSpec;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author neo
 */
public class ResponseHandler {
    private final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);
    private final ResponseHandlerContext context;
    private final ShutdownHandler shutdownHandler;

    public ResponseHandler(ResponseBeanMapper mapper, TemplateManager templateManager, ShutdownHandler shutdownHandler) {
        context = new ResponseHandlerContext(mapper, templateManager);
        this.shutdownHandler = shutdownHandler;
    }

    public void render(ResponseImpl response, HttpServerExchange exchange, ActionLog actionLog) {
        HTTPStatus status = response.status();
        exchange.setStatusCode(status.code);

        putHeaders(response, exchange);
        putCookies(response, exchange);

        closeConnectionIfShutdown(exchange);

        response.body.send(exchange.getResponseSender(), context);

        actionLog.context("responseCode", status.code);  // set response code context at last, to avoid error handler to log duplicate action_log_context key on exception
    }

    void closeConnectionIfShutdown(HttpServerExchange exchange) {
        if (shutdownHandler.shutdown.get()) {   // if during shutdown, actively close client connection, otherwise it could be reuse after server stopped due to keep alive
            exchange.setPersistent(false);
        }
    }

    private void putHeaders(ResponseImpl response, HttpServerExchange exchange) {
        HeaderMap headers = exchange.getResponseHeaders();
        for (var entry : response.headers.entrySet()) {
            HttpString name = entry.getKey();
            String value = entry.getValue();
            headers.put(name, value);
            logger.debug("[response:header] {}={}", name, new FieldLogParam(name.toString(), value));
        }
    }

    private void putCookies(ResponseImpl response, HttpServerExchange exchange) {
        if (response.cookies != null) {
            Map<String, Cookie> cookies = exchange.getResponseCookies();
            for (var entry : response.cookies.entrySet()) {
                CookieSpec spec = entry.getKey();
                String value = entry.getValue();
                CookieImpl cookie = cookie(spec, value);
                cookies.put(spec.name, cookie);
                logger.debug("[response:cookie] name={}, value={}, domain={}, path={}, secure={}, httpOnly={}, maxAge={}",
                        spec.name, new FieldLogParam(spec.name, cookie.getValue()), cookie.getDomain(), cookie.getPath(), cookie.isSecure(), cookie.isHttpOnly(), cookie.getMaxAge());
            }
        }
    }

    CookieImpl cookie(CookieSpec spec, String value) {
        var cookie = new CookieImpl(spec.name);
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
