package core.framework.http;

import core.framework.internal.http.HTTPRequestHelper;
import core.framework.util.Encodings;
import core.framework.util.Maps;
import core.framework.util.Strings;

import java.time.Duration;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class HTTPRequest {
    public final HTTPMethod method;
    public final Map<String, String> params = Maps.newLinkedHashMap();
    public final Map<String, String> headers = Maps.newLinkedHashMap();    // make headers/params order deterministic
    public String uri;
    public byte[] body;
    public ContentType contentType;
    public Map<String, String> form;    // shortcut view only, doesn't impact final request

    public Duration connectTimeout;
    public Duration timeout;            // read and write timeout
    public Duration slowOperationThreshold;

    public HTTPRequest(HTTPMethod method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public void accept(ContentType contentType) {
        headers.put(HTTPHeaders.ACCEPT, contentType.toString());
    }

    public void basicAuth(String user, String password) {
        headers.put(HTTPHeaders.AUTHORIZATION, "Basic " + Encodings.base64(user + ':' + password));
    }

    public void bearerAuth(String token) {
        headers.put(HTTPHeaders.AUTHORIZATION, "Bearer " + token);
    }

    public void body(byte[] body, ContentType contentType) {
        this.body = body;
        this.contentType = contentType;
        headers.put(HTTPHeaders.CONTENT_TYPE, contentType.toString());
    }

    public void body(String body, ContentType contentType) {
        byte[] bytes = body.getBytes(contentType.charset().orElse(UTF_8));
        body(bytes, contentType);
    }

    public void form(Map<String, String> form) {
        if (method != HTTPMethod.POST) throw new Error("form must be used with POST, method=" + method);
        this.form = form;
        var builder = new StringBuilder(256);
        HTTPRequestHelper.urlEncoding(builder, form);
        body(Strings.bytes(builder.toString()), ContentType.APPLICATION_FORM_URLENCODED);
    }

    public String requestURI() {
        if (params.isEmpty()) return uri;

        var builder = new StringBuilder(256).append(uri).append(uri.indexOf('?') > -1 ? '&' : '?');
        HTTPRequestHelper.urlEncoding(builder, params);
        return builder.toString();
    }
}
