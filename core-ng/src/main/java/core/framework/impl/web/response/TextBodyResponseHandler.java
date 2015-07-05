package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import io.undertow.server.HttpServerExchange;

/**
 * @author neo
 */
public class TextBodyResponseHandler implements BodyHandler {
    @Override
    public void handle(ResponseImpl response, HttpServerExchange exchange) {
        TextBody body = (TextBody) response.body;
        exchange.getResponseSender().send(body.text);
    }
}
