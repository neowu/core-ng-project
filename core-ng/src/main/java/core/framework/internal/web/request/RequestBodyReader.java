package core.framework.internal.web.request;

import core.framework.internal.web.HTTPIOHandler;
import core.framework.web.exception.BadRequestException;
import io.undertow.connector.PooledByteBuffer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import org.xnio.ChannelListener;
import org.xnio.IoUtils;
import org.xnio.channels.StreamSourceChannel;

import java.nio.ByteBuffer;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class RequestBodyReader implements ChannelListener<StreamSourceChannel> {
    static final AttachmentKey<RequestBody> REQUEST_BODY = AttachmentKey.create(RequestBody.class);

    private final HttpServerExchange exchange;
    private final HTTPIOHandler.Handler handler;
    private final int contentLength;
    private boolean complete;
    private byte[] body;
    private int position = 0;

    public RequestBodyReader(HttpServerExchange exchange, HTTPIOHandler.Handler handler) {
        this.exchange = exchange;
        this.handler = handler;
        contentLength = (int) exchange.getRequestContentLength();
        if (contentLength >= 0) body = new byte[contentLength];
    }

    @Override
    public void handleEvent(StreamSourceChannel channel) {
        read(channel);
        if (complete) {
            handler.handle();
        }
    }

    @SuppressWarnings("PMD.ExceptionAsFlowControl")   // intentional
    public void read(StreamSourceChannel channel) {
        try (PooledByteBuffer poolItem = exchange.getConnection().getByteBufferPool().allocate()) {
            ByteBuffer buffer = poolItem.getBuffer();
            int bytesRead;
            while (true) {
                buffer.clear();
                bytesRead = channel.read(buffer);
                if (bytesRead <= 0) break;
                buffer.flip();
                ensureCapacity(bytesRead);
                buffer.get(body, position, bytesRead);
                position += bytesRead;
            }
            if (bytesRead == -1) {
                if (contentLength >= 0 && position < body.length) {
                    throw new Error(format("body ends prematurely, expected={}, actual={}", contentLength, position));
                } else if (body == null) {
                    body = new byte[0]; // without content length and has no body
                }
                complete = true;
                exchange.putAttachment(REQUEST_BODY, new RequestBody(body, null));
            }
        } catch (Throwable e) { // catch all errors during IO, to pass error to action log
            IoUtils.safeClose(channel);
            complete = true;
            exchange.putAttachment(REQUEST_BODY, new RequestBody(null, e));
        }
    }

    private void ensureCapacity(int bytesRead) {
        if (contentLength >= 0) {
            if (bytesRead + position > contentLength) throw new Error("body exceeds expected content length, expected=" + contentLength);
        } else {
            if (body == null) { // undertow buffer is 16k, if there is no content length, in most cases, it's best just to create exact buffer as first read thru
                body = new byte[bytesRead];
            } else {
                int newLength = position + bytesRead;   // without content length, position will always be current length,
                byte[] bytes = new byte[newLength];     // just expend to exact read size, which is the simplest way for best scenario
                System.arraycopy(body, 0, bytes, 0, position);
                body = bytes;
            }
        }
    }

    public boolean complete() {
        return complete;
    }

    public static class RequestBody {
        private final byte[] body;
        private final Throwable exception;

        RequestBody(byte[] body, Throwable exception) {
            this.body = body;
            this.exception = exception;
        }

        public byte[] body() {
            if (exception != null) throw new BadRequestException(exception.getMessage(), "FAILED_TO_READ_HTTP_REQUEST", exception);
            return this.body;
        }
    }
}

