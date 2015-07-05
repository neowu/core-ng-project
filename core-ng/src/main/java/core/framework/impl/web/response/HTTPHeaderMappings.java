package core.framework.impl.web.response;

import core.framework.api.http.HTTPHeaders;
import core.framework.api.util.Maps;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.util.Map;

/**
 * @author neo
 */
class HTTPHeaderMappings {
    final Map<String, HttpString> undertowHeaderMappings = Maps.newHashMap();

    HTTPHeaderMappings() {
        undertowHeaderMappings.put(HTTPHeaders.CONTENT_TYPE, Headers.CONTENT_TYPE);
        undertowHeaderMappings.put(HTTPHeaders.LOCATION, Headers.LOCATION);
    }

    HttpString undertowHeader(String header) {
        HttpString value = undertowHeaderMappings.get(header);
        if (value != null) return value;
        return new HttpString(header);
    }
}
