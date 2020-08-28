package core.framework.internal.http;

import core.framework.http.ContentType;
import core.framework.internal.log.filter.BytesLogParam;

import static core.framework.http.ContentType.APPLICATION_JSON;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class BodyLogParam {
    public static Object of(byte[] body, ContentType contentType) {
        if (contentType != null && (APPLICATION_JSON.mediaType.equals(contentType.mediaType)
                || contentType.mediaType.contains("text")
                || contentType.mediaType.contains("xml"))) {
            return new BytesLogParam(body, contentType.charset().orElse(UTF_8));
        }
        // form is not handled here, for both http client and http server
        return "byte[" + body.length + "]";
    }
}
