package core.framework.internal.web;

import core.framework.http.ContentType;
import io.undertow.predicate.Predicate;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

import java.util.Set;

/**
 * @author neo
 */
public class GZipPredicate implements Predicate {
    // from test, roughly the compressed value becomes smaller after text larger than 100B,
    // by considering gzip cpu/ram overhead, use 200B as threshold
    private static final int MIN_GZIP_LENGTH = 200;
    private final Set<String> gzipContentTypes = Set.of(ContentType.TEXT_PLAIN.toString(),
            ContentType.TEXT_HTML.toString(),
            ContentType.TEXT_CSS.toString(),
            ContentType.TEXT_XML.toString(),
            ContentType.APPLICATION_JSON.toString(),
            ContentType.APPLICATION_JAVASCRIPT.toString());

    @Override
    public boolean resolve(HttpServerExchange exchange) {
        HeaderMap headers = exchange.getResponseHeaders();
        return resolve(headers);
    }

    boolean resolve(HeaderMap headers) {
        String contentType = headers.getFirst(Headers.CONTENT_TYPE);
        if (contentType == null || !gzipContentTypes.contains(contentType)) return false;
        String length = headers.getFirst(Headers.CONTENT_LENGTH);
        return length == null || Long.parseLong(length) > MIN_GZIP_LENGTH;
    }
}
