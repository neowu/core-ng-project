package core.framework.impl.web.service;

import core.framework.json.JSON;
import core.framework.web.exception.BadRequestException;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class PathParamParser {    // used by generated WebServiceController
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
