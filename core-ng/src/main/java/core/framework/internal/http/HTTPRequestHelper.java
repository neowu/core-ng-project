package core.framework.internal.http;

import java.util.Map;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public final class HTTPRequestHelper {
    public static void urlEncoding(StringBuilder builder, Map<String, String> params) {
        boolean added = false;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            if (added) builder.append('&');
            builder.append(encode(entry.getKey(), UTF_8)).append('=');
            // url encoding doesn't differentiate null and empty, so treat null same as empty value
            if (value != null && !value.isEmpty()) builder.append(encode(value, UTF_8));
            added = true;
        }
    }
}
