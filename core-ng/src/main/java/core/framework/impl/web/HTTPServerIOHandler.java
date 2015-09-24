package core.framework.impl.web;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Methods;

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
        // parse form body early, not process until form is read, so to save one blocking thread during read
        if (Methods.POST.equals(exchange.getRequestMethod())) {
            FormDataParser parser = formParserFactory.createParser(exchange);
            if (parser != null) {
                parser.parse(handler);
                return;
            }
        }
        handler.handleRequest(exchange);
    }
}
