package core.framework.impl.web.response;

import core.framework.api.http.HTTPStatus;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.Encodings;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.web.CookieSpec;
import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import core.framework.impl.web.request.RequestImpl;
import core.framework.impl.web.site.TemplateManager;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author neo
 */
public class ResponseHandler {
    private final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);
    private final Map<Class<? extends Body>, BodyHandler> handlers = Maps.newHashMap();

    public ResponseHandler(ResponseBeanTypeValidator validator, TemplateManager templateManager) {
        handlers.put(BeanBody.class, new BeanBodyResponseHandler(validator));
        handlers.put(TemplateBody.class, new TemplateBodyResponseHandler(templateManager));
        handlers.put(ByteArrayBody.class, new ByteArrayBodyResponseHandler());
        handlers.put(FileBody.class, new FileBodyResponseHandler());
    }

    public void handle(ResponseImpl response, HttpServerExchange exchange, RequestImpl request) {
        HTTPStatus status = response.status();
        exchange.setStatusCode(status.code);

        handleHeaders(response, exchange);

        if (response.cookies != null) {
            Map<String, Cookie> cookies = exchange.getResponseCookies();
            response.cookies.forEach((spec, value) -> {
                CookieImpl cookie = cookie(spec, value);
                logger.debug("[response:cookie] name={}, value={}, domain={}, path={}, secure={}, httpOnly={}, maxAge={}",
                        cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.isSecure(), cookie.isHttpOnly(), cookie.getMaxAge());
                cookies.put(spec.name, cookie);
            });
        }

        BodyHandler handler = handlers.get(response.body.getClass());
        if (handler == null)
            throw Exceptions.error("unexpected body class, body={}", response.body.getClass().getCanonicalName());
        logger.debug("responseHandlerClass={}", handler.getClass().getCanonicalName());
        handler.handle(response, exchange.getResponseSender(), request);

        ActionLogContext.put("responseCode", status.code);  // set response code context at last, to avoid error handler to log duplicate action_log_context key on exception
    }

    private void handleHeaders(ResponseImpl response, HttpServerExchange exchange) {
        if (response.contentType != null) {
            String contentType = response.contentType.toString();
            String previous = response.headers.put(Headers.CONTENT_TYPE, contentType);
            if (previous != null) {
                logger.warn("content type header is overwritten, value={}, previous={}", contentType, previous);
            }
        }

        HeaderMap headers = exchange.getResponseHeaders();
        response.headers.forEach((header, value) -> {
            logger.debug("[response:header] {}={}", header, value);
            headers.put(header, value);
        });
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
