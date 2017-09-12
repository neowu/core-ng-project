package core.framework.impl.web.response;

import core.framework.impl.web.request.RequestImpl;
import io.undertow.io.Sender;

import java.nio.ByteBuffer;

/**
 * @author rainbow.cai
 */
class ByteArrayBodyResponseHandler implements BodyHandler {
    @Override
    public void handle(ResponseImpl response, Sender sender, RequestImpl request) {
        ByteArrayBody body = (ByteArrayBody) response.body;
        sender.send(ByteBuffer.wrap(body.bytes));
    }
}
