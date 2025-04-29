package core.framework.search.impl;

import co.elastic.clients.transport.Endpoint;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.http.TransportHttpClient;
import co.elastic.clients.transport.instrumentation.Instrumentation;
import core.framework.internal.log.filter.BytesLogParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class ElasticSearchLogInstrumentation implements Instrumentation {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchLogInstrumentation.class);

    private final LogContext context = new LogContext();
    private final LogScope scope = new LogScope();

    @Override
    public <T> Context newContext(T request, Endpoint<T, ?, ?> endpoint) {
        return context;
    }

    byte[] readBody(Iterable<ByteBuffer> body) {
        int totalSize = 0;
        for (ByteBuffer buf : body) {
            totalSize += buf.remaining();
        }

        byte[] bytes = new byte[totalSize];
        int position = 0;

        for (ByteBuffer buf : body) {
            buf.mark();
            int remaining = buf.remaining();
            buf.get(bytes, position, remaining);
            position += remaining;
            buf.reset();
        }
        return bytes;
    }

    private static final class LogScope implements Instrumentation.ThreadScope {
        @Override
        public void close() {
        }
    }

    private final class LogContext implements Instrumentation.Context {
        @Override
        public ThreadScope makeCurrent() {
            return scope;
        }

        @Override
        public void beforeSendingHttpRequest(TransportHttpClient.Request request, TransportOptions options) {
            logger.debug("[request] method={}, path={}", request.method(), request.path());
            Iterable<ByteBuffer> body = request.body();
            if (body != null) {
                byte[] bytes = readBody(body);
                logger.debug("[request] body={}", new BytesLogParam(bytes));
            }
        }

        @Override
        public void afterReceivingHttpResponse(TransportHttpClient.Response response) {
        }

        @Override
        public <T> void afterDecodingApiResponse(T response) {
        }

        @Override
        public void recordException(Throwable e) {
        }

        @Override
        public void close() {
        }
    }
}
