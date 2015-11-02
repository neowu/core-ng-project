package core.framework.api.http;

import core.framework.api.util.ByteBuf;
import core.framework.api.util.Charsets;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class HTTPResponse {
    private final HTTPStatus status;
    private final Map<String, String> headers;
    private final ByteBuf body;
    private final ContentType contentType;
    private String text;

    public HTTPResponse(HTTPStatus status, Map<String, String> headers, ByteBuf body) {
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
        if (text == null)
            text = body.text(charset());    // cache text string if created
        return text;
    }

    public ByteBuf body() {
        return body;
    }

    private Charset charset() {
        if (contentType == null) return Charsets.UTF_8;
        return contentType.charset().orElse(Charsets.UTF_8);
    }
}
