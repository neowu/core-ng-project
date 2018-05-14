package core.framework.mongo.impl;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author neo
 */
public class LocalDateTimeCodec implements Codec<LocalDateTime> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDateTimeCodec.class);

    static void write(BsonWriter writer, LocalDateTime value) {
        if (value == null) writer.writeNull();
        else writer.writeDateTime(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    static LocalDateTime read(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.DATE_TIME) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(reader.readDateTime()), ZoneId.systemDefault());
        } else {
            LOGGER.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    @Override
    public void encode(BsonWriter writer, LocalDateTime value, EncoderContext context) {
        write(writer, value);
    }

    @Override
    public LocalDateTime decode(BsonReader reader, DecoderContext context) {
        return read(reader, reader.getCurrentName());
    }

    @Override
    public Class<LocalDateTime> getEncoderClass() {
        return LocalDateTime.class;
    }
}
