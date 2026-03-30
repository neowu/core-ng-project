package core.framework.search.impl;

import co.elastic.clients.transport.Endpoint;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.http.TransportHttpClient;
import co.elastic.clients.transport.instrumentation.Instrumentation;
import co.elastic.clients.util.BinaryData;
import core.framework.internal.http.HTTPRequestHelper;
import core.framework.internal.log.filter.FieldMapLogParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Map;

public final class LogInstrumentation implements Instrumentation {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogInstrumentation.class);
    private static final Context CONTEXT = new Context();
    private static final Scope SCOPE = new Scope();

    static String uri(String path, Map<String, String> params) {
        if (params.isEmpty()) return path;  // most of cases

        var builder = new StringBuilder(path).append('?');
        HTTPRequestHelper.urlEncoding(builder, params);
        return builder.toString();
    }

    @Override
    public <T> Instrumentation.Context newContext(T request, Endpoint<T, ?, ?> endpoint) {
        return CONTEXT;
    }

    private static final class Context implements Instrumentation.Context {
        @Override
        public ThreadScope makeCurrent() {
            return SCOPE;
        }

        @Override
        public void beforeSendingHttpRequest(TransportHttpClient.Request request, TransportOptions options) {
            LOGGER.debug("[request] method={}, uri={}", request.method(), uri(request.path(), request.queryParams()));
            LOGGER.debug("[request] headers={}", new FieldMapLogParam(request.headers()));
            Iterable<ByteBuffer> body = request.body();
            if (body != null) {
                for (ByteBuffer buffer : body) {
                    LOGGER.debug("[request] body={}", new ByteBufferParam(buffer));
                }
            }
        }

        @Override
        public void afterReceivingHttpResponse(TransportHttpClient.Response response) {
            LOGGER.debug("[response] status={}", response.statusCode());
            // currently response headers don't include useful info,
            // e.g. [response] headers={content-length=456, content-type=application/vnd.elasticsearch+json;compatible-with=9, X-elastic-product=Elasticsearch, content-encoding=gzip}
            // co.elastic.clients.transport.rest5_client.low_level.Response original = (co.elastic.clients.transport.rest5_client.low_level.Response) response.originalResponse();
            // Map<String, String> headers = Maps.newHashMapWithExpectedSize(4);
            // for (Header header : original.getHeaders()) {
            //     headers.put(header.getName(), header.getValue());
            // }
            // LOGGER.debug("[response] headers={}", new FieldMapLogParam(headers));
            try {
                BinaryData body = response.body();
                if (body != null) {
                    LOGGER.debug("[response] body={}", new ByteBufferParam(body.asByteBuffer()));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public <T> void afterDecodingApiResponse(T response) {
        }

        @Override
        public void recordException(Throwable throwable) {
        }

        @Override
        public void close() {
        }

    }

    private static final class Scope implements Instrumentation.ThreadScope {
        @Override
        public void close() {
        }
    }
}
