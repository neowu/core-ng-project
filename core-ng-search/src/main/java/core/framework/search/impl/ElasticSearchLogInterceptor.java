package core.framework.search.impl;

import core.framework.internal.log.filter.LogParam;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * @author neo
 */
public class ElasticSearchLogInterceptor implements HttpRequestInterceptor {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchLogInterceptor.class);

    // only request can be logged, apache http client execute in different thread (NIO), and response entity can only be consumed once
    @Override
    public void process(HttpRequest request, HttpContext context) {
        RequestLine requestLine = request.getRequestLine();
        logger.debug("[request] method={}, uri={}", requestLine.getMethod(), requestLine.getUri());
        if (request instanceof final HttpEntityEnclosingRequest entityRequest) {
            HttpEntity entity = entityRequest.getEntity();
            if (entity != null) {
                logger.debug("[request] body={}", new BodyParam(entity));
            }
        }
    }

    record BodyParam(HttpEntity entity) implements LogParam {
        @Override
        public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
            // refer to co.elastic.clients.transport.rest_client.RestClientTransport.prepareLowLevelRequest
            // it always uses ByteArrayEntity, thus always has content length
            try (Reader reader = new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8)) {
                char[] buffer;

                if (entity.isChunked()) {
                    // with bulkIndex, es uses co.elastic.clients.transport.rest_client.MultiBufferEntity, which is chunked and content length = -1
                    buffer = new char[maxParamLength + 1];
                } else {
                    buffer = new char[Math.min((int) entity.getContentLength(), maxParamLength + 1)];
                }
                int read = reader.read(buffer);

                if (read > 0) builder.append(buffer, 0, Math.min(read, maxParamLength));    // read = -1 if nothing read into buffer
                if (read > maxParamLength) {
                    builder.append("...(truncated)");
                }
            } catch (IOException e) {
                throw new Error(e); // not expected io exception, as it's from ByteArrayEntity
            }
        }
    }
}
