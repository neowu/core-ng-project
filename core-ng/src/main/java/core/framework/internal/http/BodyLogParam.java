package core.framework.internal.http;

import core.framework.http.ContentType;
import core.framework.impl.log.filter.BytesLogParam;
import core.framework.impl.log.filter.JSONLogParam;

import static core.framework.http.ContentType.APPLICATION_FORM_URLENCODED;
import static core.framework.http.ContentType.APPLICATION_JSON;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class BodyLogParam {
    public static Object param(byte[] body, ContentType contentType) {
        if (contentType != null) {
            if (APPLICATION_JSON.mediaType.equals(contentType.mediaType)) {
                return new JSONLogParam(body, contentType.charset().orElse(UTF_8));    // make json body filterable
            } else if (contentType.mediaType.contains("text")
                    || contentType.mediaType.contains("xml")    // for application/xml
                    || APPLICATION_FORM_URLENCODED.mediaType.equals(contentType.mediaType)) {
                return new BytesLogParam(body, contentType.charset().orElse(UTF_8));
            }
        }
        return "byte[" + body.length + "]";
    }
}
