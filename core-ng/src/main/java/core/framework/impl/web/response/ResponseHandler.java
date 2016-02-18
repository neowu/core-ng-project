package core.framework.impl.web.response;

import core.framework.api.http.HTTPStatus;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.Encodings;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.web.CookieSpec;
import core.framework.api.web.ResponseImpl;
import core.framework.api.web.site.TemplateManager;
import core.framework.impl.web.BeanValidator;
import core.framework.impl.web.request.RequestImpl;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author neo
 */
public class ResponseHandler {
    private final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);
    private final Map<Class, BodyHandler> handlers = Maps.newHashMap();

    public ResponseHandler(BeanValidator validator, TemplateManager templateManager) {
        handlers.put(BeanBody.class, new BeanBodyResponseHandler(validator));
        handlers.put(TemplateBody.class, new TemplateBodyResponseHandler(templateManager));
        handlers.put(ByteArrayBody.class, new ByteArrayBodyResponseHandler());
        handlers.put(FileBody.class, new FileBodyResponseHandler());
    }

    public void handle(ResponseImpl response, HttpServerExchange exchange, RequestImpl request) {
        HTTPStatus status = response.status();
        exchange.setStatusCode(status.code);
        ActionLogContext.put("responseCode", status.code);

        HeaderMap headers = exchange.getResponseHeaders();
        response.headers.forEach((header, value) -> {
            logger.debug("[response:header] {}={}", header, value);
            headers.put(header, value);
        });

        if (response.cookies != null) {
            Map<String, Cookie> responseCookies = exchange.getResponseCookies();
            response.cookies.forEach((spec, value) -> {
                CookieImpl cookie = cookie(spec, value);
                logger.debug("[response:cookie] name={}, value={}, domain={}, path={}, secure={}, httpOnly={}, maxAge={}",
                    cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.isSecure(), cookie.isHttpOnly(), cookie.getMaxAge());
                responseCookies.put(spec.name, cookie);
            });
        }

        BodyHandler handler = handlers.get(response.body.getClass());
        if (handler == null)
            throw Exceptions.error("unexpected body class, body={}", response.body.getClass().getCanonicalName());
        logger.debug("responseHandlerClass={}", handler.getClass().getCanonicalName());
        handler.handle(response, exchange.getResponseSender(), request);
    }

    CookieImpl cookie(CookieSpec spec, String value) {
        CookieImpl cookie = new CookieImpl(spec.name);
        if (value == null) {
            cookie.setMaxAge(0);
            cookie.setValue("");
        } else {
            if (spec.maxAge != null) cookie.setMaxAge((int) spec.maxAge.getSeconds());
            cookie.setValue(Encodings.uriComponent(value));
        }
        cookie.setDomain(spec.domain);
        cookie.setPath(spec.path);
        cookie.setSecure(spec.secure);
        cookie.setHttpOnly(spec.httpOnly);
        return cookie;
    }
}
