package core.framework.impl.web;

import core.framework.impl.web.request.RequestBodyReader;
import core.framework.util.Charsets;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import org.xnio.channels.StreamSourceChannel;

/**
 * @author neo
 */
class HTTPServerIOHandler implements HttpHandler {
    private final FormParserFactory formParserFactory;
    private final HTTPServerHandler handler;

    HTTPServerIOHandler(HTTPServerHandler handler) {
        this.handler = handler;
        FormParserFactory.Builder builder = FormParserFactory.builder();
        builder.setDefaultCharset(Charsets.UTF_8.name());
        formParserFactory = builder.build();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (hasBody(exchange)) {    // parse body early, not process until body is read (e.g. for chunked), to save one blocking thread during read
            FormDataParser parser = formParserFactory.createParser(exchange);
            if (parser != null) {
                parser.parse(handler);
                return;
            }

            var reader = new RequestBodyReader(exchange, handler);
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

    private boolean hasBody(HttpServerExchange exchange) {
        int length = (int) exchange.getRequestContentLength();
        if (length == 0) return false;  // if body is empty, skip reading

        HttpString method = exchange.getRequestMethod();
        return Methods.POST.equals(method) || Methods.PUT.equals(method) || Methods.PATCH.equals(method);
    }
}
