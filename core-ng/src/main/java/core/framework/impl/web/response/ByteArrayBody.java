package core.framework.impl.web.response;

import io.undertow.io.Sender;

import java.nio.ByteBuffer;

/**
 * @author rainbow.cai
 */
public final class ByteArrayBody implements Body {
    private final byte[] bytes;

    public ByteArrayBody(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public void send(Sender sender, ResponseHandlerContext context) {
        sender.send(ByteBuffer.wrap(bytes));
    }
}
