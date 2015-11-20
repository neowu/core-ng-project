package core.framework.impl.web.request;

import core.framework.api.util.ByteBuf;
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

    // refer to http://www.ietf.org/rfc/rfc3986.txt
    // refer to org.springframework.web.util.UriUtils#decode
    String decodePathSegment(String path) {
        int length = path.length();
        ByteBuf buffer = ByteBuf.newBuffer(length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            int ch = path.charAt(i);
            if (ch == '%') {
                if ((i + 2) >= length) throw new BadRequestException("invalid path, value=" + path.substring(i));
                char hex1 = path.charAt(i + 1);
                char hex2 = path.charAt(i + 2);
                int u = Character.digit(hex1, 16);
                int l = Character.digit(hex2, 16);
                if (u == -1 || l == -1) throw new BadRequestException("invalid path, value=" + path.substring(i));
                buffer.put((byte) ((u << 4) + l));
                i += 2;
                changed = true;
            } else {
                buffer.put((byte) ch);
            }
        }
        return changed ? buffer.text() : path;
    }
}
