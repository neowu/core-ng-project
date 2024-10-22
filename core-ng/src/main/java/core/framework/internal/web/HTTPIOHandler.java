package core.framework.internal.web;

import core.framework.internal.web.request.RequestBodyReader;
import core.framework.internal.web.sse.ServerSentEventHandler;
import core.framework.internal.web.websocket.WebSocketHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.channels.StreamSourceChannel;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class HTTPIOHandler implements HttpHandler {
    public static final String HEALTH_CHECK_PATH = "/health-check";
    private final Logger logger = LoggerFactory.getLogger(HTTPIOHandler.class);
    private final FormParserFactory formParserFactory;
    private final long maxEntitySize;
    private final HTTPHandler handler;
    private final ShutdownHandler shutdownHandler;
    private final ServerSentEventHandler sseHandler;
    private final WebSocketHandler webSocketHandler;

    HTTPIOHandler(HTTPHandler handler, ShutdownHandler shutdownHandler, long maxEntitySize, ServerSentEventHandler sseHandler, WebSocketHandler webSocketHandler) {
        this.handler = handler;
        this.shutdownHandler = shutdownHandler;
        formParserFactory = createFormParserFactory();
        this.maxEntitySize = maxEntitySize;
        this.sseHandler = sseHandler;
        this.webSocketHandler = webSocketHandler;
    }

    private FormParserFactory createFormParserFactory() {
        var builder = FormParserFactory.builder(false);

        var formDefinition = new FormEncodedDataDefinition();
        formDefinition.setDefaultEncoding(UTF_8.name());
        builder.addParser(formDefinition);

        var multiPartDefinition = new MultiPartParserDefinition();
        multiPartDefinition.setDefaultEncoding(UTF_8.name());
        multiPartDefinition.setFileSizeThreshold(-1); // always write to temp file for multipart uploading
        builder.addParser(multiPartDefinition);

        return builder.build();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String path = exchange.getRequestPath();
        if (HEALTH_CHECK_PATH.equals(path)) {      // not treat health-check as action
            handler.addKeepAliveHeader(exchange);
            exchange.endExchange(); // end exchange will send 200 / content-length=0
            return;
        }

        long contentLength = exchange.getRequestContentLength();
        if (!checkContentLength(contentLength, exchange)) return;

        HttpString method = exchange.getRequestMethod();
        HeaderMap headers = exchange.getRequestHeaders();
        boolean sse = sseHandler != null && sseHandler.check(method, headers, path);
        boolean ws = webSocketHandler != null && webSocketHandler.check(method, headers);
        boolean active = !sse && !ws;
        boolean shutdown = shutdownHandler.handle(exchange, active);
        if (shutdown) return;

        if (hasBody(contentLength, method)) {    // parse body early, not process until body is read (e.g. for chunked), to save one blocking thread during read
            FormDataParser parser = formParserFactory.createParser(exchange);   // no need to close, refer to io.undertow.server.handlers.form.MultiPartParserDefinition.create, it closes on ExchangeCompletionListener
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

        if (active) {
            exchange.dispatch(handler);
        } else if (sse) {
            sseHandler.handleRequest(exchange); // not dispatch, continue in io thread
        } else {
            exchange.dispatch(webSocketHandler);
        }
    }

    // undertow is not handling max entity size checking correctly, it terminates request directly and bypass exchange.endExchange() in certain cases, and log errors in debug level
    // thus ExchangeCompleteListener will not be triggered, and cause problem on graceful shutdown
    // for http/1.1, refer to io.undertow.conduits.FixedLengthStreamSourceConduit.checkMaxSize,
    //      io.undertow.server.handlers.form.MultiPartParserDefinition.MultiPartUploadHandler.NonBlockingParseTask
    // for http/2.0, refer to io.undertow.server.protocol.framed.AbstractFramedStreamSourceChannel.handleStreamTooLarge
    //
    // here to mitigate the issue by checking content length before shutdown handler
    //
    // for H2 and "Transfer-Encoding: chunked", there is no easy way to handle graceful shutdown correctly, this is flaw of current undertow h2 implementation
    boolean checkContentLength(long contentLength, HttpServerExchange exchange) {
        if (contentLength > maxEntitySize) {
            logger.warn("content length is too large, requestURL={}, contentLength={}, maxEntitySize={}", exchange.getRequestURL(), contentLength, maxEntitySize);
            exchange.setStatusCode(413);    // 413 Payload Too Large (RFC 7231), it doesn't matter as undertow terminates connection, and browser will see protocol error
            exchange.endExchange();
            return false;
        }
        return true;
    }

    boolean hasBody(long contentLength, HttpString method) {
        if (contentLength == 0) return false;  // if body is empty, skip reading
        return Methods.POST.equals(method) || Methods.PUT.equals(method) || Methods.PATCH.equals(method);
    }
}
