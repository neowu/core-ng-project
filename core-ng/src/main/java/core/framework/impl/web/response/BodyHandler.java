package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import io.undertow.server.HttpServerExchange;

/**
 * @author neo
 */
public interface BodyHandler {
    void handle(ResponseImpl response, HttpServerExchange exchange);
}
