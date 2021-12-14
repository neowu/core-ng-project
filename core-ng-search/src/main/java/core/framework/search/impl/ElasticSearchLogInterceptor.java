package core.framework.search.impl;

import core.framework.internal.log.filter.LogParam;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (entity != null) {
                logger.debug("[request] body={}", new BodyParam(entity));
            }
        }
    }

    private record BodyParam(HttpEntity entity) implements LogParam {
        @Override
        public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
            try {
                builder.append(EntityUtils.toString(entity));
            } catch (IOException e) {
                throw new Error(e);
            }
        }
    }
}
