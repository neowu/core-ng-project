package core.framework.internal.web.service;

import core.framework.json.JSON;
import core.framework.util.Encodings;
import core.framework.web.exception.BadRequestException;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class PathParamHelper {    // used by generated WebServiceController and WebServiceClient
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

    public static String toString(String value) {
        return Encodings.uriComponent(value);
    }

    public static String toString(Number value) {
        return Encodings.uriComponent(String.valueOf(value));
    }

    public static <T extends Enum<?>> String toString(T enumValue) {
        return Encodings.uriComponent(JSON.toEnumValue(enumValue));
    }
}
