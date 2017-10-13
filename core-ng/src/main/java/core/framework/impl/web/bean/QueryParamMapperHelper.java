package core.framework.impl.web.bean;

import core.framework.impl.web.request.URLParamParser;
import core.framework.json.JSON;
import core.framework.util.Strings;
import core.framework.web.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author neo
 */
final class QueryParamMapperHelper {   // used by generated QueryParamMapper
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

    public static String toString(ZonedDateTime dateTime) {
        if (dateTime == null) return null;
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
    }

    public static <T extends Enum<?>> String toString(T enumValue) {
        if (enumValue == null) return null;
        return JSON.toEnumValue(enumValue);
    }

    public static Integer toInt(String value) {
        if (Strings.isEmpty(value)) return null;
        return URLParamParser.toInt(value);
    }

    public static Long toLong(String value) {
        if (Strings.isEmpty(value)) return null;
        return URLParamParser.toLong(value);
    }

    public static Double toDouble(String value) {
        if (Strings.isEmpty(value)) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException(Strings.format("failed to parse double, value={}", value), BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }

    public static BigDecimal toBigDecimal(String value) {
        if (Strings.isEmpty(value)) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException(Strings.format("failed to parse big decimal, value={}", value), BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }

    public static Boolean toBoolean(String value) {
        if (Strings.isEmpty(value)) return null;
        return URLParamParser.toBoolean(value);
    }

    public static <T extends Enum<?>> T toEnum(String value, Class<T> valueClass) {
        if (Strings.isEmpty(value)) return null;
        return URLParamParser.toEnum(value, valueClass);
    }

    public static ZonedDateTime toZonedDateTime(String value) {
        if (Strings.isEmpty(value)) return null;
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(Strings.format("failed to parse zoned date time, value={}", value), BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }

    public static LocalDateTime toDateTime(String value) {
        if (Strings.isEmpty(value)) return null;
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(Strings.format("failed to parse local date time, value={}", value), BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }

    public static LocalDate toDate(String value) {
        if (Strings.isEmpty(value)) return null;
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(Strings.format("failed to parse local date, value={}", value), BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }
}
