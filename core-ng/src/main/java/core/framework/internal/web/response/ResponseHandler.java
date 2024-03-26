package core.framework.internal.web.response;

import core.framework.api.http.HTTPStatus;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.filter.FieldLogParam;
import core.framework.internal.web.bean.ResponseBeanWriter;
import core.framework.internal.web.request.RequestImpl;
import core.framework.internal.web.session.SessionManager;
import core.framework.internal.web.site.TemplateManager;
import core.framework.util.Encodings;
import core.framework.web.CookieSpec;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.CookieSameSiteMode;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ResponseHandler {
    private final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);
    private final ResponseHandlerContext context;
    private final SessionManager sessionManager;

    public ResponseHandler(ResponseBeanWriter writer, TemplateManager templateManager, SessionManager sessionManager) {
        context = new ResponseHandlerContext(writer, templateManager);
        this.sessionManager = sessionManager;
    }

    public void render(RequestImpl request, ResponseImpl response, HttpServerExchange exchange, ActionLog actionLog) {
        // always try to save session before response, even for exception flow in case it's invalidated or generated new sessionId
        sessionManager.save(request, response, actionLog);

        HTTPStatus status = response.status();
        exchange.setStatusCode(status.code);
        actionLog.context.put("response_code", List.of(String.valueOf(status.code)));
        logger.debug("[response] statusCode={}", status.code);

        putHeaders(response, exchange);
        putCookies(response, exchange);

        long bodyLength = response.body.send(exchange.getResponseSender(), context);
        // due to exchange.sender is async, exchange.getResponseBytesSent() won't return accurate body length at this point,
        // use actual body bytes size prior to gzip, to match http client perf_http stats
        logger.debug("[response] bodyLength={}", bodyLength);
        actionLog.stats.put("response_body_length", (double) bodyLength);
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
            for (Map.Entry<CookieSpec, String> entry : response.cookies.entrySet()) {
                CookieSpec spec = entry.getKey();
                String value = entry.getValue();
                CookieImpl cookie = cookie(spec, value);
                exchange.setResponseCookie(cookie);
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
        // refer to https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#samesite-cookie-attribute,
        // lax is good enough for common scenario, as long as webapp doesn't make sensitive side effect thru TOP LEVEL navigation
        if (spec.sameSite) cookie.setSameSiteMode(CookieSameSiteMode.LAX.toString());
        return cookie;
    }

    String cookieKey(CookieSpec spec) {
        return spec.name + ":" + spec.domain + ":" + spec.path;
    }
}
