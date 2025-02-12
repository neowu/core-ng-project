package core.framework.internal.web;

import core.framework.internal.web.sse.ServerSentEventHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

public class HTTPRequestHandler {
    final boolean sse;
    private final HttpServerExchange exchange;
    private final HTTPHandler handler;
    private final ServerSentEventHandler sseHandler;

    HTTPRequestHandler(HttpServerExchange exchange, HTTPHandler handler, ServerSentEventHandler sseHandler) {
        this.exchange = exchange;
        this.handler = handler;
        this.sseHandler = sseHandler;

        HttpString method = exchange.getRequestMethod();
        String path = exchange.getRequestPath();
        HeaderMap headers = exchange.getRequestHeaders();
        sse = sseHandler != null && sseHandler.check(method, path, headers);
    }

    public void handle() {
        if (sse) {
            sseHandler.handleRequest(exchange); // not dispatch, continue in io thread
        } else {
            exchange.dispatch(handler);
        }
    }
}
