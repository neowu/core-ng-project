package core.framework.impl.http;

import java.util.Map;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class HTTPRequestHelper {
    public static void urlEncoding(StringBuilder builder, Map<String, String> params) {
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) builder.append('&');
            builder.append(encode(entry.getKey(), UTF_8)).append('=').append(encode(entry.getValue(), UTF_8));
            first = false;
        }
    }
}
