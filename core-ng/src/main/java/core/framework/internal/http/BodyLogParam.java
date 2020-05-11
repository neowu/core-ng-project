package core.framework.internal.http;

import core.framework.http.ContentType;
import core.framework.internal.log.filter.BytesLogParam;
import core.framework.internal.log.filter.JSONLogParam;

import static core.framework.http.ContentType.APPLICATION_JSON;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class BodyLogParam {
    public static Object of(byte[] body, ContentType contentType) {
        if (contentType != null) {
            if (APPLICATION_JSON.mediaType.equals(contentType.mediaType)) {
                return new JSONLogParam(body, contentType.charset().orElse(UTF_8));    // make json body filterable
            } else if (contentType.mediaType.contains("text")
                    || contentType.mediaType.contains("xml")) { // for application/xml
                return new BytesLogParam(body, contentType.charset().orElse(UTF_8));
            }   // form is not handled here, for both http client and http server
        }
        return "byte[" + body.length + "]";
    }
}
