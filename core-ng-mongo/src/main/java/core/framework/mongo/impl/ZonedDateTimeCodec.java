package core.framework.mongo.impl;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
public class ZonedDateTimeCodec implements Codec<ZonedDateTime> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZonedDateTimeCodec.class);

    static void write(BsonWriter writer, @Nullable ZonedDateTime value) {
        if (value == null) writer.writeNull();
        else writer.writeDateTime(value.toInstant().toEpochMilli());
    }

    @Nullable
    static ZonedDateTime read(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.DATE_TIME) {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(reader.readDateTime()), ZoneId.systemDefault());
        } else {
            LOGGER.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    @Override
    public void encode(BsonWriter writer, ZonedDateTime value, EncoderContext context) {
        write(writer, value);
    }

    @Nullable
    @Override
    public ZonedDateTime decode(BsonReader reader, DecoderContext context) {
        return read(reader, reader.getCurrentName());
    }

    @Override
    public Class<ZonedDateTime> getEncoderClass() {
        return ZonedDateTime.class;
    }
}
