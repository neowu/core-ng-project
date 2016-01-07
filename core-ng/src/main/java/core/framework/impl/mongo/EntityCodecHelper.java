package core.framework.impl.mongo;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * @author neo
 */
// used by generated encoder and decoder
public final class EntityCodecHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityCodecHelper.class);

    public static void writeObjectId(BsonWriter writer, ObjectId value) {
        if (value == null) writer.writeNull();
        else writer.writeObjectId(value);
    }

    public static void writeString(BsonWriter writer, String value) {
        if (value == null) writer.writeNull();
        else writer.writeString(value);
    }

    public static void writeInteger(BsonWriter writer, Integer value) {
        if (value == null) writer.writeNull();
        else writer.writeInt32(value);
    }

    public static void writeLong(BsonWriter writer, Long value) {
        if (value == null) writer.writeNull();
        else writer.writeInt64(value);
    }

    public static void writeDouble(BsonWriter writer, Double value) {
        if (value == null) writer.writeNull();
        else writer.writeDouble(value);
    }

    public static void writeBoolean(BsonWriter writer, Boolean value) {
        if (value == null) writer.writeNull();
        else writer.writeBoolean(value);
    }

    public static void writeLocalDateTime(BsonWriter writer, LocalDateTime value) {
        if (value == null) writer.writeNull();
        else LocalDateTimeCodec.write(writer, value);
    }

    public static void writeEnum(BsonWriter writer, Enum value) {
        if (value == null) writer.writeNull();
        else writer.writeString(String.valueOf(value));
    }

    public static Integer readInteger(BsonReader reader, BsonType currentType, String field) {
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.INT32) {
            return reader.readInt32();
        } else {
            LOGGER.warn("field returned from mongo is ignored, field={}", field);
            reader.skipValue();
            return null;
        }
    }

    public static ObjectId readObjectId(BsonReader reader, BsonType currentType, String field) {
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.OBJECT_ID) {
            return reader.readObjectId();
        } else {
            LOGGER.warn("field returned from mongo is ignored, field={}", field);
            reader.skipValue();
            return null;
        }
    }

    public static Long readLong(BsonReader reader, BsonType currentType, String field) {
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.INT64) {
            return reader.readInt64();
        } else {
            LOGGER.warn("field returned from mongo is ignored, field={}", field);
            reader.skipValue();
            return null;
        }
    }

    public static String readString(BsonReader reader, BsonType currentType, String field) {
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.STRING) {
            return reader.readString();
        } else {
            LOGGER.warn("field returned from mongo is ignored, field={}", field);
            reader.skipValue();
            return null;
        }
    }

    public static Double readDouble(BsonReader reader, BsonType currentType, String field) {
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.DOUBLE) {
            return reader.readDouble();
        } else {
            LOGGER.warn("field returned from mongo is ignored, field={}", field);
            reader.skipValue();
            return null;
        }
    }

    public static Boolean readBoolean(BsonReader reader, BsonType currentType, String field) {
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.BOOLEAN) {
            return reader.readBoolean();
        } else {
            LOGGER.warn("field returned from mongo is ignored, field={}", field);
            reader.skipValue();
            return null;
        }
    }

    public static Object readEnum(BsonReader reader, BsonType currentType, Class enumClass, String field) {
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.STRING) {
            String value = reader.readString();
            Object[] enums = enumClass.getEnumConstants();
            for (Object item : enums) {
                if (String.valueOf(item).equals(value)) return item;
            }
            LOGGER.warn("enum returned from mongo is ignored, enumClass={}, value={}, field={}", enumClass, value, field);
            return null;
        } else {
            LOGGER.warn("field returned from mongo is ignored, field={}", field);
            reader.skipValue();
            return null;
        }
    }

    public static LocalDateTime readLocalDateTime(BsonReader reader, BsonType currentType, String field) {
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.DATE_TIME) {
            return LocalDateTimeCodec.read(reader);
        } else {
            LOGGER.warn("field returned from mongo is ignored, field={}", field);
            reader.skipValue();
            return null;
        }
    }
}
