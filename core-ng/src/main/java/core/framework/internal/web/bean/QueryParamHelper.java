package core.framework.internal.web.bean;

import core.framework.internal.web.service.PathParamHelper;
import core.framework.json.JSON;
import core.framework.web.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author neo
 */
final class QueryParamHelper {   // used by generated QueryParamMapper
    public static String toString(Number value) {
        if (value == null) return null;
        return value.toString();
    }

    public static String toString(Boolean value) {
        if (value == null) return null;
        return value.toString();
    }

    public static String toString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
    }

    public static String toString(LocalDate date) {
        if (date == null) return null;
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    public static String toString(LocalTime time) {
        if (time == null) return null;
        return DateTimeFormatter.ISO_LOCAL_TIME.format(time);
    }

    public static String toString(ZonedDateTime dateTime) {
        if (dateTime == null) return null;
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
    }

    public static <T extends Enum<?>> String toString(T enumValue) {
        if (enumValue == null) return null;
        return JSON.toEnumValue(enumValue);
    }

    // deserialization helpers
    public static String toString(String value) {
        if (value.isEmpty()) return null;
        return value;
    }

    public static Integer toInt(String value) {
        if (value.isEmpty()) return null;
        return PathParamHelper.toInt(value);
    }

    public static Long toLong(String value) {
        if (value.isEmpty()) return null;
        return PathParamHelper.toLong(value);
    }

    public static Double toDouble(String value) {
        if (value.isEmpty()) return null;
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("failed to parse double, value=" + value, "INVALID_HTTP_REQUEST", e);
        }
    }

    public static BigDecimal toBigDecimal(String value) {
        if (value.isEmpty()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("failed to parse big decimal, value=" + value, "INVALID_HTTP_REQUEST", e);
        }
    }

    public static Boolean toBoolean(String value) {
        if (value.isEmpty()) return null;
        return Boolean.valueOf(value);  // Boolean.parseBoolean does not throw exception
    }

    public static <T extends Enum<?>> T toEnum(String value, Class<T> valueClass) {
        if (value.isEmpty()) return null;
        return PathParamHelper.toEnum(value, valueClass);
    }

    public static ZonedDateTime toZonedDateTime(String value) {
        if (value.isEmpty()) return null;
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("failed to parse zoned date time, value=" + value, "INVALID_HTTP_REQUEST", e);
        }
    }

    public static LocalDateTime toDateTime(String value) {
        if (value.isEmpty()) return null;
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("failed to parse local date time, value=" + value, "INVALID_HTTP_REQUEST", e);
        }
    }

    public static LocalTime toTime(String value) {
        if (value.isEmpty()) return null;
        try {
            return LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("failed to parse local time, value=" + value, "INVALID_HTTP_REQUEST", e);
        }
    }

    public static LocalDate toDate(String value) {
        if (value.isEmpty()) return null;
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("failed to parse local date, value=" + value, "INVALID_HTTP_REQUEST", e);
        }
    }
}
