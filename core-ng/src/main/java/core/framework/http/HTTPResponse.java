package core.framework.http;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class HTTPResponse {
    public final int statusCode;
    public final byte[] body;
    public final Map<String, String> headers;   // headers key is case insensitive
    @Nullable
    public final ContentType contentType;

    public HTTPResponse(int statusCode, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;

        String contentType = headers.get(HTTPHeaders.CONTENT_TYPE);
        this.contentType = contentType == null ? null : ContentType.parse(contentType);
    }

    public String text() {
        Charset charset = UTF_8;
        if (contentType != null) charset = contentType.charset().orElse(UTF_8);
        return new String(body, charset);
    }
}
