package core.framework.mongo.impl;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public final class BsonReaderWrapper {      // used by generated entity decoder
    private final Logger logger = LoggerFactory.getLogger(BsonReaderWrapper.class);
    private final BsonReader reader;

    public BsonReaderWrapper(BsonReader reader) {
        this.reader = reader;
    }

    public Integer readInteger(String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.INT32) {
            return reader.readInt32();
        } else {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public ObjectId readObjectId(String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.OBJECT_ID) {
            return reader.readObjectId();
        } else {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public Long readLong(String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.INT32) {
            return (long) reader.readInt32();
        } else if (currentType == BsonType.INT64) {
            return reader.readInt64();
        } else {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public String readString(String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.STRING) {
            return reader.readString();
        } else {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public Double readDouble(String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.DOUBLE) {
            return reader.readDouble();
        } else {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public Boolean readBoolean(String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.BOOLEAN) {
            return reader.readBoolean();
        } else {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    public LocalDateTime readLocalDateTime(String field) {
        return LocalDateTimeCodec.read(reader, field);
    }

    public ZonedDateTime readZonedDateTime(String field) {
        return ZonedDateTimeCodec.read(reader, field);
    }

    public List<?> startReadList(String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (currentType != BsonType.ARRAY) {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
        return new ArrayList<>();
    }

    public Map<String, ?> startReadMap(String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (currentType != BsonType.DOCUMENT) {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
        return new LinkedHashMap<>();
    }

    public boolean startReadEntity(String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType != null && currentType == BsonType.NULL) {
            reader.readNull();
            return false;
        }
        if (currentType != null && currentType != BsonType.DOCUMENT) {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return false;
        }
        return true;
    }
}
