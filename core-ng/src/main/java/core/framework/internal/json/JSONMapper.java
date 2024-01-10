package core.framework.internal.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.StreamWriteFeature;
import com.fasterxml.jackson.core.util.JsonRecyclerPools;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
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
import java.util.HashMap;
import java.util.Map;

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
    public static final ObjectMapper OBJECT_MAPPER = builder().build();
    private static Map<Class<?>, JSONReader<?>> readers = new HashMap<>();
    private static Map<Class<?>, JSONWriter<?>> writers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> JSONReader<T> reader(Class<T> beanClass) {
        // can only be used during config time within module, App will run cleanup after startup
        return (JSONReader<T>) readers.computeIfAbsent(beanClass, JSONReader::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> JSONWriter<T> writer(Class<T> beanClass) {
        // can only be used during config time within module, App will run cleanup after startup
        return (JSONWriter<T>) writers.computeIfAbsent(beanClass, JSONWriter::new);
    }

    public static void cleanup() {
        readers = null;
        writers = null;
    }

    // expose builder, to allow app build its own JSON mapper to parse external json, e.g. can be less strict
    public static JsonMapper.Builder builder() {
        JsonFactory jsonFactory = JsonFactory.builder()
            .recyclerPool(JsonRecyclerPools.sharedLockFreePool())
            .build();

        // refer to com.fasterxml.jackson.databind.ObjectMapper.DEFAULT_BASE for default settings, e.g. cacheProvider
        return JsonMapper.builder(jsonFactory)
            .addModule(timeModule())
            .defaultDateFormat(new StdDateFormat())
            // disable value class loader to avoid jdk illegal reflection warning, requires JSON class/fields must be public
            .addModule(new AfterburnerModule().setUseValueClassLoader(false))
            // only detect public fields, refer to com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std
            .visibility(new VisibilityChecker.Std(NONE, NONE, NONE, NONE, PUBLIC_ONLY))
            .enable(StreamReadFeature.USE_FAST_DOUBLE_PARSER)
            .enable(StreamReadFeature.USE_FAST_BIG_NUMBER_PARSER)
            .enable(StreamWriteFeature.USE_FAST_DOUBLE_WRITER)
            .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            // e.g. disable convert empty string to Integer null
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .annotationIntrospector(new JSONAnnotationIntrospector())
            .deactivateDefaultTyping();
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
