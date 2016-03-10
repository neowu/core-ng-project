package core.framework.impl.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * @author neo
 */
public class EnumCodec<T extends Enum<T>> implements Codec<T> {
    private final Class<T> enumClass;

    public EnumCodec(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext context) {
        writer.writeString(value.name());
    }

    @Override
    public T decode(BsonReader reader, DecoderContext context) {
        return Enum.valueOf(enumClass, reader.readString());
    }

    @Override
    public Class<T> getEncoderClass() {
        return enumClass;
    }
}
