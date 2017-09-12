package core.framework.impl.web.response;

import core.framework.impl.log.LogParam;
import core.framework.impl.web.request.RequestImpl;
import io.undertow.io.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author rainbow.cai
 */
class ByteArrayBodyResponseHandler implements BodyHandler {
    private final Logger logger = LoggerFactory.getLogger(ByteArrayBodyResponseHandler.class);

    @Override
    public void handle(ResponseImpl response, Sender sender, RequestImpl request) {
        ByteArrayBody body = (ByteArrayBody) response.body;
        if (response.contentType != null)
            response.contentType.charset().ifPresent(charset -> logger.debug("[response] body={}", LogParam.of(body.bytes, charset)));
        sender.send(ByteBuffer.wrap(body.bytes));
    }
}
