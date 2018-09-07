package core.framework.impl.web.response;

import io.undertow.io.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author rainbow.cai
 */
public final class ByteArrayBody implements Body {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayBody.class);
    private final byte[] bytes;

    public ByteArrayBody(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public void send(Sender sender, ResponseHandlerContext context) {
        LOGGER.debug("[response] body=bytes[{}]", bytes.length);
        sender.send(ByteBuffer.wrap(bytes));
    }
}
