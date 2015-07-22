package core.framework.impl.web.response;

import core.framework.api.web.ResponseImpl;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.DateUtils;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.MimeMappings;
import org.xnio.IoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.util.Date;

/**
 * @author neo
 */
public class FileBodyResponseHandler implements BodyHandler {
    @Override
    public void handle(ResponseImpl response, HttpServerExchange exchange) {
        File file = ((FileBody) response.body).file;
        HeaderMap headers = exchange.getResponseHeaders();

        String fileName = file.getName();
        int index = fileName.lastIndexOf('.');
        if (index > 0 && index + 1 < fileName.length()) {
            String extension = fileName.substring(index + 1);
            String contentType = MimeMappings.DEFAULT.getMimeType(extension);
            headers.put(Headers.CONTENT_TYPE, contentType);
        }

        headers.put(Headers.LAST_MODIFIED, DateUtils.toDateString(new Date(file.lastModified())));

        try {
            FileChannel channel = new FileInputStream(file).getChannel();
            exchange.getResponseSender().transferFrom(channel, new IoCallback() {
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
