package core.framework.impl.mongo;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import org.bson.codecs.Codec;

import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public final class EntityCodecs {
    final Map<Class<?>, EntityCodec<?>> codecs = Maps.newHashMap();
    final Set<Class<? extends Enum>> enumClasses = Sets.newHashSet();

    public <T> void registerEntity(Class<T> entityClass) {
        EntityIdHandler<T> entityIdHandler = new EntityIdHandlerBuilder<>(entityClass).build();
        register(entityClass, entityIdHandler);
    }

    public <T> void registerView(Class<T> viewClass) {
        register(viewClass, null);
    }

    public <T> Object id(T entity) {
        @SuppressWarnings("unchecked")
        EntityCodec<T> codec = (EntityCodec<T>) codecs.get(entity.getClass());
        EntityIdHandler<T> idHandler = codec.idHandler;
        return idHandler.get(entity);
    }

    private <T> void register(Class<T> entityClass, EntityIdHandler<T> idHandler) {
        EntityEncoderBuilder<T> builder = new EntityEncoderBuilder<>(entityClass);
        EntityEncoder<T> entityEncoder = builder.build();
        enumClasses.addAll(builder.enumClasses);
        EntityDecoder<T> entityDecoder = new EntityDecoderBuilder<>(entityClass).build();
        EntityCodec<T> codec = new EntityCodec<>(entityClass, idHandler, entityEncoder, entityDecoder);
        Codec<?> previous = codecs.putIfAbsent(entityClass, codec);
        if (previous != null)
            throw Exceptions.error("entity or view class is registered, entityClass={}", entityClass.getCanonicalName());
    }
}
