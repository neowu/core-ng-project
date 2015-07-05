package core.framework.api.http;

import core.framework.api.util.Charsets;
import org.apache.http.entity.ContentType;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class HTTPResponse {
    final HTTPStatus status;
    final Map<String, String> headers;
    final byte[] bytes;
    String responseText;

    public HTTPResponse(HTTPStatus status, Map<String, String> headers, byte[] bytes) {
        this.status = status;
        this.headers = headers;
        this.bytes = bytes;
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

    public String contentType() {
        return headers.get(HTTPHeaders.CONTENT_TYPE);
    }

    public String text() {
        if (responseText == null)
            responseText = new String(bytes, charset());
        return responseText;
    }

    public byte[] bytes() {
        return bytes;
    }

    private Charset charset() {
        String contentTypeValue = contentType();
        if (contentTypeValue == null) return Charsets.UTF_8;
        ContentType contentType = ContentType.parse(contentTypeValue);
        Charset charset = contentType.getCharset();
        if (charset == null) return Charsets.UTF_8;
        return charset;
    }
}
