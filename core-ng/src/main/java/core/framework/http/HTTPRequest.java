package core.framework.http;

import core.framework.impl.http.HTTPRequestHelper;
import core.framework.util.Encodings;
import core.framework.util.Maps;
import core.framework.util.Strings;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class HTTPRequest {
    // due to history issue, many web server may have issue if send charset=utf-8 explicitly, so not use charset
    private static final ContentType APPLICATION_FORM_URLENCODED = ContentType.create("application/x-www-form-urlencoded", null);

    private final String uri;
    private final HTTPMethod method;
    private final Map<String, String> headers = Maps.newLinkedHashMap();    // make headers/params order deterministic, (e.g. for use cases where http request needs to be signed by hash)
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

    public void param(String name, String value) {
        params.put(name, value);
    }

    public byte[] body() {
        return body;
    }

    public void body(String body, ContentType contentType) {
        byte[] bytes = body.getBytes(contentType.charset().orElse(UTF_8));
        body(bytes, contentType);
    }

    public void body(byte[] body, ContentType contentType) {
        this.body = body;
        this.contentType = contentType;
    }

    public void form(Map<String, String> form) {
        var builder = new StringBuilder(256);
        HTTPRequestHelper.urlEncoding(builder, form);
        body(Strings.bytes(builder.toString()), APPLICATION_FORM_URLENCODED);
    }

    public ContentType contentType() {
        return contentType;
    }
}
