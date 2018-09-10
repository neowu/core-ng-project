package core.framework.mongo.impl;

import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.IdGenerator;
import org.bson.codecs.ObjectIdGenerator;
import org.bson.types.ObjectId;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
final class EntityCodec<T> implements CollectibleCodec<T> {
    final EntityIdHandler<T> idHandler;
    private final Class<T> entityClass;
    private final EntityEncoder<T> encoder;
    private final EntityDecoder<T> decoder;
    private final IdGenerator idGenerator = new ObjectIdGenerator();

    EntityCodec(Class<T> entityClass, EntityIdHandler<T> idHandler, EntityEncoder<T> encoder, EntityDecoder<T> decoder) {
        this.entityClass = entityClass;
        this.idHandler = idHandler;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public T generateIdIfAbsentFromDocument(T document) {
        if (!documentHasId(document)) {
            if (!idHandler.generateIdIfAbsent())
                throw new Error(format("id must be assigned, documentClass={}", document.getClass().getCanonicalName()));
            idHandler.set(document, idGenerator.generate());
        }
        return document;
    }

    @Override
    public boolean documentHasId(T document) {
        return idHandler.get(document) != null;
    }

    @Override
    public BsonValue getDocumentId(T document) {
        Object id = idHandler.get(document);
        if (id instanceof ObjectId) return new BsonObjectId((ObjectId) id);
        if (id instanceof String) new BsonString((String) id);
        throw new Error(format("unsupported id type, id={}", id));
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        encoder.encode(writer, value);
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        return decoder.decode(reader);
    }

    @Override
    public Class<T> getEncoderClass() {
        return entityClass;
    }
}
