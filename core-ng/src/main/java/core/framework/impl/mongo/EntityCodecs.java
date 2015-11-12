package core.framework.impl.mongo;

import com.mongodb.MongoClient;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author neo
 */
public final class EntityCodecs {
    public final Map<Class<?>, EntityCodec<?>> codecs = Maps.newHashMap();

    public <T> void entityClass(Class<T> entityClass) {
        EntityIdHandler<T> entityIdHandler = new EntityIdHandlerBuilder<>(entityClass).build();
        registerCodec(entityClass, entityIdHandler);
    }

    public <T> void viewClass(Class<T> viewClass) {
        new MongoClassValidator(viewClass).validateViewClass();
        registerCodec(viewClass, null);
    }

    private <T> void registerCodec(Class<T> entityClass, EntityIdHandler<T> idHandler) {
        EntityEncoder<T> entityEncoder = new EntityEncoderBuilder<>(entityClass).build();
        EntityDecoder<T> entityDecoder = new EntityDecoderBuilder<>(entityClass).build();
        EntityCodec<T> codec = new EntityCodec<>(entityClass, idHandler, entityEncoder, entityDecoder);
        Codec<?> previous = codecs.putIfAbsent(entityClass, codec);
        if (previous != null)
            throw Exceptions.error("entity or view class is registered, entityClass={}", entityClass.getCanonicalName());
    }

    public CodecRegistry codecRegistry() {
        return CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
            CodecRegistries.fromCodecs(new ArrayList<>(codecs.values())));
    }

    public <T> Object id(T entity) {
        @SuppressWarnings("unchecked")
        EntityCodec<T> codec = (EntityCodec<T>) codecs.get(entity.getClass());
        EntityIdHandler<T> idHandler = codec.idHandler;
        return idHandler.get(entity);
    }
}
