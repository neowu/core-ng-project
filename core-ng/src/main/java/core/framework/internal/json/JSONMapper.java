package core.framework.internal.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

/**
 * @author neo
 */
public class JSONMapper {
    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .addModule(timeModule())
                // disable value class loader to avoid jdk illegal reflection warning, requires JSON class/fields must be public
                .addModule(new AfterburnerModule().setUseValueClassLoader(false))
                .defaultDateFormat(new StdDateFormat())
                // only auto detect field, and default visibility is public_only, refer to com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std
                .visibility(new VisibilityChecker.Std(NONE, NONE, NONE, NONE, PUBLIC_ONLY))
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .annotationIntrospector(new JSONAnnotationIntrospector())
                .deactivateDefaultTyping()
                .build();
    }

    private static JavaTimeModule timeModule() {
        var module = new JavaTimeModule();

        // redefine date time formatter to output nano seconds in at least 3 digits, which inline with ISO standard and ES standard
        DateTimeFormatter localTimeFormatter = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .appendFraction(NANO_OF_SECOND, 3, 9, true) // always output 3 digits of nano seconds (iso date format doesn't specify how many digits it should present, here always keep 3)
                .toFormatter();

        module.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(ISO_INSTANT));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(new DateTimeFormatterBuilder()
                .parseStrict()
                .append(ISO_LOCAL_DATE)
                .appendLiteral('T')
                .append(localTimeFormatter)
                .toFormatter()));
        module.addSerializer(LocalTime.class, new LocalTimeSerializer(new DateTimeFormatterBuilder()
                .parseStrict()
                .append(localTimeFormatter)
                .toFormatter()));
        return module;
    }
}
