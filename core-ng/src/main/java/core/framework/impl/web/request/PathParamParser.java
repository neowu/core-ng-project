package core.framework.impl.web.request;

import core.framework.json.JSON;
import core.framework.web.exception.BadRequestException;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class PathParamParser {
    @SuppressWarnings("unchecked")
    static <T> T parse(String param, Class<T> valueClass) {
        if (String.class.equals(valueClass)) {
            return (T) param;
        } else if (Integer.class.equals(valueClass)) {
            return (T) toInt(param);
        } else if (Long.class.equals(valueClass)) {
            return (T) toLong(param);
        } else if (valueClass.isEnum()) {
            return (T) toEnum(param, (Class<? extends Enum<?>>) valueClass);
        }
        throw new Error("not supported path param type, type=" + valueClass.getCanonicalName());
    }

    public static <T extends Enum<?>> T toEnum(String value, Class<T> valueClass) {
        try {
            return JSON.fromEnumValue(valueClass, value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(format("failed to parse enum, enumClass={}, value={}", valueClass.getCanonicalName(), value), "INVALID_HTTP_REQUEST", e);
        }
    }

    public static Long toLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("failed to parse long, value=" + value, "INVALID_HTTP_REQUEST", e);
        }
    }

    public static Integer toInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("failed to parse int, value=" + value, "INVALID_HTTP_REQUEST", e);
        }
    }
}
