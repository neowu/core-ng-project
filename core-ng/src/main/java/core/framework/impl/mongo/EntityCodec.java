package core.framework.impl.mongo;

import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.IdGenerator;
import org.bson.codecs.ObjectIdGenerator;
import org.bson.types.ObjectId;

/**
 * @author neo
 */
public class EntityCodec<T> implements CollectibleCodec<T> {
    private final Class<T> entityClass;
    private final EntityEncoder<T> entityEncoder;
    private final EntityDecoder<T> entityDecoder;
    private final EntityIdHandler<T> entityIdHandler;
    private final IdGenerator idGenerator = new ObjectIdGenerator();

    public EntityCodec(Class<T> entityClass, EntityIdHandler<T> entityIdHandler, EntityEncoder<T> entityEncoder, EntityDecoder<T> entityDecoder) {
        this.entityClass = entityClass;
        this.entityIdHandler = entityIdHandler;
        this.entityEncoder = entityEncoder;
        this.entityDecoder = entityDecoder;
    }

    @Override
    public T generateIdIfAbsentFromDocument(T document) {
        entityIdHandler.set(document, (ObjectId) idGenerator.generate());
        return document;
    }

    @Override
    public boolean documentHasId(T document) {
        return entityIdHandler.get(document) != null;
    }

    @Override
    public BsonValue getDocumentId(T document) {
        return new BsonObjectId(entityIdHandler.get(document));
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        entityEncoder.encode(writer, value);
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        return entityDecoder.decode(reader);
    }

    @Override
    public Class<T> getEncoderClass() {
        return entityClass;
    }
}
