package core.framework.impl.mongo;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import org.bson.codecs.Codec;

import java.util.Map;

/**
 * @author neo
 */
public final class EntityCodecs {
    public final Map<Class<?>, EntityCodec<?>> codecs = Maps.newHashMap();

    public <T> void entityClass(Class<T> entityClass) {
        EntityIdHandler<T> entityIdHandler = new EntityIdHandlerBuilder<>(entityClass).build();
        register(entityClass, entityIdHandler);
    }

    public <T> void viewClass(Class<T> viewClass) {
        new MongoClassValidator(viewClass).validateViewClass();
        register(viewClass, null);
    }

    public <T> Object id(T entity) {
        @SuppressWarnings("unchecked")
        EntityCodec<T> codec = (EntityCodec<T>) codecs.get(entity.getClass());
        EntityIdHandler<T> idHandler = codec.idHandler;
        return idHandler.get(entity);
    }

    private <T> void register(Class<T> entityClass, EntityIdHandler<T> idHandler) {
        EntityEncoder<T> entityEncoder = new EntityEncoderBuilder<>(entityClass).build();
        EntityDecoder<T> entityDecoder = new EntityDecoderBuilder<>(entityClass).build();
        EntityCodec<T> codec = new EntityCodec<>(entityClass, idHandler, entityEncoder, entityDecoder);
        Codec<?> previous = codecs.putIfAbsent(entityClass, codec);
        if (previous != null)
            throw Exceptions.error("entity or view class is registered, entityClass={}", entityClass.getCanonicalName());
    }
}
