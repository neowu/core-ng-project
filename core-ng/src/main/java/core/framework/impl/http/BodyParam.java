package core.framework.impl.http;

import core.framework.http.ContentType;
import core.framework.impl.log.filter.BytesParam;
import core.framework.impl.log.filter.JSONParam;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class BodyParam {
    public static Object param(byte[] body, ContentType contentType) {
        if (contentType != null) {
            String mediaType = contentType.mediaType();
            if (ContentType.APPLICATION_JSON.mediaType().equals(mediaType)) {
                return new JSONParam(body, contentType.charset().orElse(UTF_8));    // make json body filterable
            } else if (mediaType.contains("text")) {
                return new BytesParam(body);
            }
        }
        return "byte[" + body.length + "]";
    }
}
