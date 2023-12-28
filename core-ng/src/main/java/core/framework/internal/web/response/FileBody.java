package core.framework.internal.web.response;

import core.framework.log.ErrorCode;
import core.framework.log.Severity;
import core.framework.util.Files;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;

import java.io.IOException;
import java.io.Serial;
import java.io.UncheckedIOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author neo
 */
public final class FileBody implements Body {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBody.class);
    private final Path path;

    public FileBody(Path path) {
        this.path = path;
    }

    @Override
    public long send(Sender sender, ResponseHandlerContext context) {
        LOGGER.debug("[response] file={}", path);
        try {
            long size = Files.size(path);
            FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
            sender.transferFrom(channel, new FileBodyCallback(channel));
            return size;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static class FileBodyCallback implements IoCallback {
        private final FileChannel channel;

        FileBodyCallback(FileChannel channel) {
            this.channel = channel;
        }

        @Override
        public void onComplete(HttpServerExchange exchange, Sender sender) {
            IoUtils.safeClose(channel);
            END_EXCHANGE.onComplete(exchange, sender);
        }

        @Override
        public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
            IoUtils.safeClose(channel);
            END_EXCHANGE.onException(exchange, sender, exception);
            throw convertException(exception);
        }

        UncheckedIOException convertException(IOException exception) {
            // convert client abort exception to warning, e.g. user closed browser before content is transferred completely
            if (exception instanceof ClosedChannelException) {
                return new ClientAbortException(exception);
            }
            return new UncheckedIOException(exception);
        }
    }

    static class ClientAbortException extends UncheckedIOException implements ErrorCode {
        @Serial
        private static final long serialVersionUID = 3981103270777664274L;

        ClientAbortException(IOException cause) {
            super(cause);
        }

        @Override
        public String errorCode() {
            return "CLIENT_ABORT";
        }

        @Override
        public Severity severity() {
            return Severity.WARN;
        }
    }
}
