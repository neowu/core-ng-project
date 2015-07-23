package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import core.framework.impl.web.RequestImpl;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import org.xnio.IoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;

/**
 * @author neo
 */
public class FileBodyResponseHandler implements BodyHandler {
    @Override
    public void handle(ResponseImpl response, Sender sender, RequestImpl request) {
        File file = ((FileBody) response.body).file;

        try {
            FileChannel channel = new FileInputStream(file).getChannel();
            sender.transferFrom(channel, new IoCallback() {
                @Override
                public void onComplete(HttpServerExchange exchange, Sender sender) {
                    IoUtils.safeClose(channel);
                }

                @Override
                public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
                    IoUtils.safeClose(channel);
                    throw new UncheckedIOException(exception);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
