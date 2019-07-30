package core.framework.internal.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import core.framework.util.Strings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

/**
 * @author neo
 */
public class JSONMapper<T> {
    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(timeModule());
        mapper.registerModule(new AfterburnerModule().setUseValueClassLoader(false));   // disable value class loader to avoid jdk illegal reflection warning, requires JSON class/fields must be public
        mapper.setDateFormat(new StdDateFormat());
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setAnnotationIntrospector(new JSONAnnotationIntrospector());
        return mapper;
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

    // used internally, performance is top priority in design, reader is about 3~6% faster than mapper since type is pre determined
    // refer to https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance
    private final ObjectReader reader;
    private final ObjectWriter writer;

    public JSONMapper(Type instanceType) {
        JavaType type = OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
        reader = OBJECT_MAPPER.readerFor(type);
        writer = OBJECT_MAPPER.writerFor(type);
    }

    public T fromJSON(byte[] json) {
        try {
            return reader.readValue(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // with jdk 11, write to String then covert to byte[] is faster than write to byte[]
    public byte[] toJSON(T instance) {
        try {
            return Strings.bytes(writer.writeValueAsString(instance));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
