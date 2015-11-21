package core.framework.impl.web.request;

import core.framework.api.util.ByteBuf;
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
        String previousValue = params.putIfAbsent(name, decodePathSegment(value));
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
            throw new BadRequestException("failed to parse value to long, value=" + value, BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }

    private Integer toInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("failed to parse value to int, value=" + value, BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }

    // refer to http://www.ietf.org/rfc/rfc3986.txt, org.springframework.web.util.UriUtils#decode
    String decodePathSegment(String path) {
        int length = path.length();
        int index = 0;
        for (; index < length; index++) {
            int ch = path.charAt(index);
            if (ch == '%') break;
        }
        if (index == length) return path;
        ByteBuf buffer = ByteBuf.newBuffer(length);
        for (int i = 0; i < index; i++) buffer.put((byte) path.charAt(i));
        for (; index < length; index++) {
            int ch = path.charAt(index);
            if (ch == '%') {
                if ((index + 2) >= length) throw new BadRequestException("invalid path, value=" + path.substring(index));
                char hex1 = path.charAt(index + 1);
                char hex2 = path.charAt(index + 2);
                int u = Character.digit(hex1, 16);
                int l = Character.digit(hex2, 16);
                if (u == -1 || l == -1) throw new BadRequestException("invalid path, value=" + path.substring(index));
                buffer.put((byte) ((u << 4) + l));
                index += 2;
            } else {
                buffer.put((byte) ch);
            }
        }
        return buffer.text();
    }
}
