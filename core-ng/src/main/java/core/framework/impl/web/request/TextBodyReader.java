package core.framework.impl.web.request;

import core.framework.api.util.Charsets;
import core.framework.impl.web.HTTPServerHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.ChannelListener;
import org.xnio.IoUtils;
import org.xnio.Pooled;
import org.xnio.channels.StreamSourceChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author neo
 */
public class TextBodyReader implements ChannelListener<StreamSourceChannel> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextBodyReader.class);
    static final AttachmentKey<TextBody> TEXT_BODY = AttachmentKey.create(TextBody.class);

    private final HttpServerExchange exchange;
    private final HTTPServerHandler handler;
    private byte[] body;
    private int position;
    private boolean finished;

    public TextBodyReader(HttpServerExchange exchange, HTTPServerHandler handler) {
        this.exchange = exchange;
        this.handler = handler;
        int length = (int) exchange.getRequestContentLength();
        if (length < 0) length = 8192;  // 8k for chunked request
        body = new byte[length];
    }

    @Override
    public void handleEvent(StreamSourceChannel channel) {
        try {
            read(channel);
            if (finished) {
                exchange.dispatch(handler);
            }
        } catch (IOException e) {
            LOGGER.error("failed to read request from channel", e);
            IoUtils.safeClose(channel);
            exchange.setResponseCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.endExchange();
        }
    }

    public void read(StreamSourceChannel channel) throws IOException {
        int bytesRead;
        try (Pooled<ByteBuffer> poolItem = exchange.getConnection().getBufferPool().allocate()) {
            ByteBuffer buffer = poolItem.getResource();
            do {
                buffer.clear();
                bytesRead = channel.read(buffer);
                if (bytesRead > 0) {
                    buffer.flip();
                    int size = buffer.remaining();
                    if (size + position > body.length) {
                        int newLength = body.length * 2;
                        while (size + position > newLength) newLength = newLength * 2;
                        body = Arrays.copyOf(body, newLength);
                    }
                    buffer.get(body, position, size);
                    position += size;
                }
            } while (bytesRead > 0);
            if (bytesRead == -1) {
                finished = true;
                exchange.putAttachment(TEXT_BODY, new TextBody(new String(body, 0, position, Charsets.UTF_8)));
            }
        }
    }

    public boolean finished() {
        return finished;
    }

    public static class TextBody {
        public final String content;

        public TextBody(String content) {
            this.content = content;
        }
    }
}

