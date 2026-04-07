package core.framework.internal.json;

import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.Version;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.util.JsonRecyclerPools;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.ZonedDateTimeSerializer;
import tools.jackson.databind.introspect.VisibilityChecker;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleSerializers;
import tools.jackson.databind.util.StdDateFormat;
import tools.jackson.module.afterburner.AfterburnerModule;

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
    public static final JsonMapper OBJECT_MAPPER = builder().build();
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
            .recyclerPool(JsonRecyclerPools.sharedConcurrentDequePool())
            .build();

        // refer to com.fasterxml.jackson.databind.ObjectMapper.DEFAULT_BASE for default settings, e.g. cacheProvider
        return JsonMapper.builder(jsonFactory)
            .addModule(timeModule())
            .defaultDateFormat(new StdDateFormat())
            // disable value class loader to avoid jdk illegal reflection warning, requires JSON class/fields must be public
            .addModule(new AfterburnerModule().setUseValueClassLoader(false))
            // only detect public fields, refer to com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std
            .changeDefaultVisibility(_ -> new VisibilityChecker(PUBLIC_ONLY, NONE, NONE, NONE, NONE, NONE))
            .enable(StreamWriteFeature.USE_FAST_DOUBLE_WRITER)
            .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            // e.g. disable convert empty string to Integer null
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .enable(EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
            .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .annotationIntrospector(new JSONAnnotationIntrospector())
            .deactivateDefaultTyping();
    }

    public static JacksonModule timeModule() {
        return new TimeModule();
    }

    private static final class TimeModule extends JacksonModule {
        @Override
        public String getModuleName() {
            return "time";
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public void setupModule(SetupContext context) {
            // redefine date time formatter to output nanoseconds in at least 3 digits, which inline with ISO standard and ES standard
            DateTimeFormatter localTimeFormatter = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .appendFraction(NANO_OF_SECOND, 3, 9, true) // always output 3 digits of nanoseconds (iso date format doesn't specify how many digits it should present, here always keep 3)
                .toFormatter();

            context.addSerializers(new SimpleSerializers()
                .addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(ISO_INSTANT))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(new DateTimeFormatterBuilder()
                    .parseStrict()
                    .append(ISO_LOCAL_DATE)
                    .appendLiteral('T')
                    .append(localTimeFormatter)
                    .toFormatter()))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(new DateTimeFormatterBuilder()
                    .parseStrict()
                    .append(localTimeFormatter)
                    .toFormatter()))
            );
        }
    }
}
