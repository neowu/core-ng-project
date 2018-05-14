package core.framework.mongo.impl;

import org.bson.BsonWriter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
public final class BsonWriterWrapper {   // used by generated entity encoder
    private final BsonWriter writer;

    public BsonWriterWrapper(BsonWriter writer) {
        this.writer = writer;
    }

    public void write(ObjectId value) {
        if (value == null) writer.writeNull();
        else writer.writeObjectId(value);
    }

    public void write(String value) {
        if (value == null) writer.writeNull();
        else writer.writeString(value);
    }

    public void write(Integer value) {
        if (value == null) writer.writeNull();
        else writer.writeInt32(value);
    }

    public void write(Long value) {
        if (value == null) writer.writeNull();
        else writer.writeInt64(value);
    }

    public void write(Double value) {
        if (value == null) writer.writeNull();
        else writer.writeDouble(value);
    }

    public void write(Boolean value) {
        if (value == null) writer.writeNull();
        else writer.writeBoolean(value);
    }

    public void write(LocalDateTime value) {
        LocalDateTimeCodec.write(writer, value);
    }

    public void write(ZonedDateTime value) {
        ZonedDateTimeCodec.write(writer, value);
    }
}
