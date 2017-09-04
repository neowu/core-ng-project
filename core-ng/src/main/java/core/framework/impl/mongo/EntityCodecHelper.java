package core.framework.impl.mongo;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
public final class EntityCodecHelper {      // used by generated entity encoder and decoder
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityCodecHelper.class);

    public static Integer readInteger(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.INT32) {
            return reader.readInt32();
        } else {
            LOGGER.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public static ObjectId readObjectId(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.OBJECT_ID) {
            return reader.readObjectId();
        } else {
            LOGGER.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public static Long readLong(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.INT64) {
            return reader.readInt64();
        } else {
            LOGGER.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public static String readString(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.STRING) {
            return reader.readString();
        } else {
            LOGGER.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public static Double readDouble(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.DOUBLE) {
            return reader.readDouble();
        } else {
            LOGGER.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public static Boolean readBoolean(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.BOOLEAN) {
            return reader.readBoolean();
        } else {
            LOGGER.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public static LocalDateTime readLocalDateTime(BsonReader reader, String field) {
        return LocalDateTimeCodec.read(reader, field);
    }

    public static ZonedDateTime readZonedDateTime(BsonReader reader, String field) {
        return ZonedDateTimeCodec.read(reader, field);
    }
}
