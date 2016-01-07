package core.framework.impl.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author neo
 */
public class LocalDateTimeCodec implements Codec<LocalDateTime> {
    static void write(BsonWriter writer, LocalDateTime value) {
        writer.writeDateTime(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    static LocalDateTime read(BsonReader reader) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(reader.readDateTime()), ZoneId.systemDefault());
    }

    @Override
    public void encode(BsonWriter writer, LocalDateTime value, EncoderContext encoderContext) {
        write(writer, value);
    }

    @Override
    public LocalDateTime decode(BsonReader reader, DecoderContext decoderContext) {
        return read(reader);
    }

    @Override
    public Class<LocalDateTime> getEncoderClass() {
        return LocalDateTime.class;
    }
}
