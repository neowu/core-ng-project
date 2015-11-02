package core.framework.impl.web.request;

import core.framework.api.util.ByteBuf;
import core.framework.impl.web.HTTPServerHandler;
import io.undertow.connector.PooledByteBuffer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import org.xnio.ChannelListener;
import org.xnio.IoUtils;
import org.xnio.channels.StreamSourceChannel;

import java.nio.ByteBuffer;

/**
 * @author neo
 */
public class RequestBodyReader implements ChannelListener<StreamSourceChannel> {
    static final AttachmentKey<RequestBody> REQUEST_BODY = AttachmentKey.create(RequestBody.class);

    private final HttpServerExchange exchange;
    private final HTTPServerHandler handler;
    private ByteBuf body;
    private boolean complete;

    public RequestBodyReader(HttpServerExchange exchange, HTTPServerHandler handler) {
        this.exchange = exchange;
        this.handler = handler;
        int length = (int) exchange.getRequestContentLength();
        if (length < 0) body = ByteBuf.newBuffer();
        else body = ByteBuf.newBufferWithExpectedLength(length);
    }

    @Override
    public void handleEvent(StreamSourceChannel channel) {
        read(channel);
        if (complete) {
            exchange.dispatch(handler);
        }
    }

    public void read(StreamSourceChannel channel) {
        try (PooledByteBuffer poolItem = exchange.getConnection().getByteBufferPool().allocate()) {
            ByteBuffer buffer = poolItem.getBuffer();
            int bytesRead;
            while (true) {
                buffer.clear();
                bytesRead = channel.read(buffer);
                if (bytesRead <= 0) break;
                buffer.flip();
                body.read(buffer);
            }
            if (bytesRead == -1) {
                complete = true;
                exchange.putAttachment(REQUEST_BODY, new RequestBody(body, null));
            }
        } catch (Throwable e) { // catch all errors during IO, to pass error to action log
            IoUtils.safeClose(channel);
            complete = true;
            exchange.putAttachment(REQUEST_BODY, new RequestBody(null, e));
        }
    }

    public boolean complete() {
        return complete;
    }

    public static class RequestBody {
        private final ByteBuf body;
        private final Throwable exception;

        RequestBody(ByteBuf body, Throwable exception) {
            this.body = body;
            this.exception = exception;
        }

        public ByteBuf body() throws Throwable {
            if (exception != null) throw exception;
            return this.body;
        }
    }
}

