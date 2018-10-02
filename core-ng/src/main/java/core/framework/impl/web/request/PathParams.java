package core.framework.impl.web.request;

import core.framework.util.Encodings;
import core.framework.util.Maps;
import core.framework.web.exception.BadRequestException;

import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class PathParams {
    private final Map<String, String> params = Maps.newHashMap();

    public void put(String name, String value) {
        if (value.isEmpty()) throw new BadRequestException(format("path param must not be empty, name={}, value={}", name, value), "INVALID_HTTP_REQUEST");
        // value here is not decoded, refer to core.framework.impl.web.request.RequestParser.path
        // decodeURIComponent won't throw exception, if the uri is invalid, undertow decoding will break first and return 400
        params.put(name, Encodings.decodeURIComponent(value));
    }

    public String get(String name) {
        String value = params.get(name);
        if (value == null) throw new Error("path param not found, name=" + name);
        return value;
    }
}
