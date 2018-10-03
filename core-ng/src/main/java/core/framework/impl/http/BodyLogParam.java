package core.framework.impl.http;

import core.framework.http.ContentType;
import core.framework.impl.log.filter.BytesLogParam;
import core.framework.impl.log.filter.JSONLogParam;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class BodyLogParam {
    public static Object param(byte[] body, ContentType contentType) {
        if (contentType != null) {
            String mediaType = contentType.mediaType();
            if (ContentType.APPLICATION_JSON.mediaType().equals(mediaType)) {
                return new JSONLogParam(body, contentType.charset().orElse(UTF_8));    // make json body filterable
            } else if (mediaType.contains("text")
                    || ContentType.APPLICATION_FORM_URLENCODED.mediaType().equals(mediaType)) {
                return new BytesLogParam(body);
            }
        }
        return "byte[" + body.length + "]";
    }
}
