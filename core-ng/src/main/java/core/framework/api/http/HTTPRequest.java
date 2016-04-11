package core.framework.api.http;

import core.framework.api.util.Charsets;
import core.framework.impl.log.LogParam;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class HTTPRequest {
    public static HTTPRequest get(String uri) {
        return new HTTPRequest(HTTPMethod.GET, uri);
    }

    public static HTTPRequest post(String uri) {
        return new HTTPRequest(HTTPMethod.POST, uri);
    }

    public static HTTPRequest put(String uri) {
        return new HTTPRequest(HTTPMethod.PUT, uri);
    }

    public static HTTPRequest delete(String uri) {
        return new HTTPRequest(HTTPMethod.DELETE, uri);
    }

    final RequestBuilder builder;
    private final Logger logger = LoggerFactory.getLogger(HTTPRequest.class);

    public HTTPRequest(HTTPMethod method, String uri) {
        logger.debug("[request] method={}, uri={}", method, uri);
        try {
            builder = RequestBuilder.create(method.name()).setUri(uri);
        } catch (IllegalArgumentException e) {
            throw new HTTPClientException(e.getMessage(), "INVALID_URL", e);
        }
    }

    public HTTPRequest accept(ContentType contentType) {
        return header(HTTPHeaders.ACCEPT, contentType.toString());
    }

    public HTTPRequest header(String name, String value) {
        logger.debug("[request:header] {}={}", name, value);
        builder.setHeader(name, value);
        return this;
    }

    public HTTPRequest addParam(String name, String value) {
        logger.debug("[request:param] {}={}", name, value);
        builder.addParameter(name, value);
        return this;
    }

    public HTTPRequest body(String body, ContentType contentType) {
        byte[] bytes = body.getBytes(contentType.charset().orElse(Charsets.UTF_8));
        return body(bytes, contentType);
    }

    public HTTPRequest body(byte[] body, ContentType contentType) {
        logger.debug("[request] contentType={}, body={}", contentType, LogParam.of(body));
        org.apache.http.entity.ContentType type = org.apache.http.entity.ContentType.create(contentType.mediaType(), contentType.charset().orElse(null));
        builder.setEntity(new ByteArrayEntity(body, type));
        return this;
    }
}
