package core.framework.api.http;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class HTTPRequest {
    final RequestBuilder builder;
    private final Logger logger = LoggerFactory.getLogger(HTTPRequest.class);
    private final String uri;
    private String body;

    public HTTPRequest(HTTPMethod method, String uri) {
        logger.debug("[request] method={}, uri={}", method, uri);
        this.uri = uri;
        builder = RequestBuilder.create(method.name()).setUri(uri);
    }

    public static HTTPRequest get(String uri) {
        return new HTTPRequest(HTTPMethod.GET, uri);
    }

    public static HTTPRequest post(String uri) {
        return new HTTPRequest(HTTPMethod.POST, uri);
    }

    public HTTPRequest accept(String contentType) {
        return header(HTTPHeaders.ACCEPT, contentType);
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

    public HTTPRequest text(String body, String contentType) {
        logger.debug("[request] body={}, contentType={}", body, contentType);
        this.body = body;
        builder.setEntity(new StringEntity(body, ContentType.parse(contentType)));
        return this;
    }

    public String uri() {
        return uri;
    }

    public String body() {
        return body;
    }
}
