package core.framework.http;

import core.framework.util.Charsets;
import core.framework.util.Encodings;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public final class HTTPRequest {
    private final String uri;
    private final HTTPMethod method;
    private final Map<String, String> headers = Maps.newLinkedHashMap();
    private final Map<String, String> params = Maps.newLinkedHashMap();
    private ContentType contentType;
    private byte[] body;

    public HTTPRequest(HTTPMethod method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public String uri() {
        return uri;
    }

    public HTTPMethod method() {
        return method;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public void header(String name, String value) {
        headers.put(name, value);
    }

    public void accept(ContentType contentType) {
        header(HTTPHeaders.ACCEPT, contentType.toString());
    }

    public void basicAuth(String user, String password) {
        header(HTTPHeaders.AUTHORIZATION, "Basic " + Encodings.base64(user + ':' + password));
    }

    public Map<String, String> params() {
        return params;
    }

    public void addParam(String name, String value) {
        params.put(name, value);
    }

    public byte[] body() {
        return body;
    }

    public void body(String body, ContentType contentType) {
        byte[] bytes = body.getBytes(contentType.charset().orElse(Charsets.UTF_8));
        body(bytes, contentType);
    }

    public void body(byte[] body, ContentType contentType) {
        this.body = body;
        this.contentType = contentType;
    }

    public ContentType contentType() {
        return contentType;
    }
}
