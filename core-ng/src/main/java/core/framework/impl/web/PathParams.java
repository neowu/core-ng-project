package core.framework.impl.web;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.web.exception.BadRequestException;

import java.util.Map;

/**
 * @author neo
 */
public class PathParams {
    final Map<String, String> params = Maps.newHashMap();

    public void put(String name, String value) {
        String previousValue = params.putIfAbsent(name, value);
        if (previousValue != null) throw Exceptions.error("duplicated path variable found, name={}", name);
    }

    public <T> T get(String name, Class<T> valueClass) {
        String value = params.get(name);
        if (value == null) throw Exceptions.error("path variable not found, name={}", name);
        return convert(value, valueClass);
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(String value, Class<T> valueClass) {
        if (String.class.equals(valueClass)) {
            return (T) value;
        } else if (Integer.class.equals(valueClass)) {
            return (T) toInt(value);
        } else if (Long.class.equals(valueClass)) {
            return (T) toLong(value);
        }
        throw Exceptions.error("not supported path param type, please contact arch team, type={}", valueClass);
    }

    private Long toLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("failed to parse value to long, value=" + value, e);
        }
    }

    private Integer toInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("failed to parse value to int, value=" + value, e);
        }
    }
}
