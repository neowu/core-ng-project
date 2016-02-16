package core.framework.api.http;

import core.framework.api.util.Charsets;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class HTTPResponse {
    final ContentType contentType;
    private final HTTPStatus status;
    private final Map<String, String> headers;
    private final byte[] body;

    public HTTPResponse(HTTPStatus status, Map<String, String> headers, byte[] body) {
        this.status = status;
        this.headers = headers;
        this.body = body;

        String contentType = headers.get(HTTPHeaders.CONTENT_TYPE);
        this.contentType = contentType == null ? null : ContentType.parse(contentType);
    }

    public HTTPStatus status() {
        return status;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Optional<String> header(String name) {
        return Optional.ofNullable(headers.get(name));
    }

    public Optional<ContentType> contentType() {
        return Optional.ofNullable(contentType);
    }

    public String text() {
        return new String(body, charset());
    }

    public byte[] body() {
        return body;
    }

    private Charset charset() {
        if (contentType == null) return Charsets.UTF_8;
        return contentType.charset().orElse(Charsets.UTF_8);
    }
}
