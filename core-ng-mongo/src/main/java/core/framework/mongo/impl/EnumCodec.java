package core.framework.mongo.impl;

import core.framework.impl.reflect.Enums;
import core.framework.mongo.MongoEnumValue;
import core.framework.util.Maps;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class EnumCodec<T extends Enum<T>> implements Codec<T> {
    private final Logger logger = LoggerFactory.getLogger(EnumCodec.class);

    private final Class<T> enumClass;
    private final EnumMap<T, String> encodingMappings;
    private final Map<String, T> decodingMappings;

    public EnumCodec(Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        this.enumClass = enumClass;
        encodingMappings = new EnumMap<>(enumClass);
        decodingMappings = Maps.newHashMapWithExpectedSize(constants.length);
        for (T constant : constants) {
            String value = Enums.constantAnnotation(constant, MongoEnumValue.class).value();
            encodingMappings.put(constant, value);
            decodingMappings.put(value, constant);
        }
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext context) {
        if (value == null) {
            writer.writeNull();
        } else {
            writer.writeString(encodingMappings.get(value));
        }
    }

    // used by EntityDecoder
    public T read(BsonReader reader, String field) {
        BsonType currentType = reader.getCurrentBsonType();
        if (currentType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (currentType == BsonType.STRING) {
            String enumValue = reader.readString();
            T value = decodingMappings.get(enumValue);
            if (value == null) throw new Error(format("can not decode value to enum, enumClass={}, value={}", enumClass.getCanonicalName(), enumValue));
            return value;
        } else {
            logger.warn("unexpected field type, field={}, type={}", field, currentType);
            reader.skipValue();
            return null;
        }
    }

    @Override
    public T decode(BsonReader reader, DecoderContext context) {
        return read(reader, reader.getCurrentName());
    }

    @Override
    public Class<T> getEncoderClass() {
        return enumClass;
    }
}
