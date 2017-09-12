package core.framework.impl.web.response;

import core.framework.impl.web.request.RequestImpl;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author neo
 */
class FileBodyResponseHandler implements BodyHandler {
    private final Logger logger = LoggerFactory.getLogger(FileBodyResponseHandler.class);

    @Override
    public void handle(ResponseImpl response, Sender sender, RequestImpl request) {
        Path path = ((FileBody) response.body).path;
        logger.debug("[response] file={}", path);
        try {
            FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
            sender.transferFrom(channel, new FileBodyCallback(channel));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class FileBodyCallback implements IoCallback {
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
            throw new UncheckedIOException(exception);
        }
    }
}
