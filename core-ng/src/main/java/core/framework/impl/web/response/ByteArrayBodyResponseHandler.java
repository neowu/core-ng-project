package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import io.undertow.server.HttpServerExchange;

import java.nio.ByteBuffer;

/**
 * @author rainbow.cai
 */
public class ByteArrayBodyResponseHandler implements BodyHandler {
    @Override
    public void handle(ResponseImpl response, HttpServerExchange exchange) {
        ByteArrayBody body = (ByteArrayBody) response.body;
        exchange.getResponseSender().send(ByteBuffer.wrap(body.bytes));
    }
}
