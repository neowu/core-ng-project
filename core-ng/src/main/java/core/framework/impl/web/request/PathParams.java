package core.framework.impl.web.request;

import core.framework.api.util.Encodings;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.web.exception.BadRequestException;

import java.util.Map;

/**
 * @author neo
 */
public final class PathParams {
    final Map<String, String> params = Maps.newHashMap();

    public void put(String name, String value) {
        if (value.length() == 0) throw new BadRequestException("path param must not be empty, name=" + name + ", value=" + value);
        try {
            params.put(name, Encodings.decodeURIComponent(value));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage(), BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }

    public String get(String name) {
        String value = params.get(name);
        if (value == null) throw Exceptions.error("path variable not found, name={}", name);
        return value;
    }
}
