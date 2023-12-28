package core.framework.mongo.impl;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * @author neo
 */
public class LocalDateCodec implements Codec<LocalDate> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDateCodec.class);

    static void write(BsonWriter writer, LocalDate value) {
        if (value == null) writer.writeNull();
        else writer.writeString(value.toString());
    }

    static LocalDate read(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.STRING) {
            String value = reader.readString();
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                LOGGER.warn("invalid local date format, field={}, value={}", field, value);
                return null;
            }
        } else {
            LOGGER.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    @Override
    public void encode(BsonWriter writer, LocalDate value, EncoderContext context) {
        write(writer, value);
    }

    @Override
    public LocalDate decode(BsonReader reader, DecoderContext context) {
        return read(reader, reader.getCurrentName());
    }

    @Override
    public Class<LocalDate> getEncoderClass() {
        return LocalDate.class;
    }
}
