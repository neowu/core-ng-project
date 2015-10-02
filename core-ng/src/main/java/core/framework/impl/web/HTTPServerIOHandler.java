package core.framework.impl.web;

import core.framework.impl.web.request.TextBodyReader;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import org.xnio.channels.StreamSourceChannel;

/**
 * @author neo
 */
public class HTTPServerIOHandler implements HttpHandler {
    private final FormParserFactory formParserFactory = FormParserFactory.builder().build();
    private final HTTPServerHandler handler;

    public HTTPServerIOHandler(HTTPServerHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // parse form body early, not process until body is read (e.g. for chunked), so to save one blocking thread during read
        HttpString method = exchange.getRequestMethod();
        if (Methods.POST.equals(method)) {
            FormDataParser parser = formParserFactory.createParser(exchange);
            if (parser != null) {
                parser.parse(handler);
                return;
            }
        }

        if (hasTextBody(exchange)) {
            TextBodyReader reader = new TextBodyReader(exchange, handler);
            StreamSourceChannel channel = exchange.getRequestChannel();
            reader.read(channel);  // channel will be null if getRequestChannel() is already called, but here should not be that case
            if (!reader.complete()) {
                channel.getReadSetter().set(reader);
                channel.resumeReads();
                return;
            }
        }

        exchange.dispatch(handler);
    }

    private boolean hasTextBody(HttpServerExchange exchange) {
        int length = (int) exchange.getRequestContentLength();
        if (length == 0) return false;  // if body is empty, skip reading

        HttpString method = exchange.getRequestMethod();
        if (!Methods.POST.equals(method) && !Methods.PUT.equals(method)) return false;

        String contentType = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
        return contentType != null && contentType.startsWith("application/json");
    }
}
