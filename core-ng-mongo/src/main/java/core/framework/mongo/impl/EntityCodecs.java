package core.framework.mongo.impl;

import core.framework.util.Maps;
import core.framework.util.Sets;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public final class EntityCodecs {
    private final Map<Class<?>, EntityCodec<?>> codecs = Maps.newHashMap();
    private final Set<Class<? extends Enum<?>>> enumClasses = Sets.newHashSet();

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
        enumClasses.addAll(builder.enumCodecFields.keySet());
        EntityDecoder<T> entityDecoder = new EntityDecoderBuilder<>(entityClass).build();
        EntityCodec<T> codec = new EntityCodec<>(entityClass, idHandler, entityEncoder, entityDecoder);
        Codec<?> previous = codecs.putIfAbsent(entityClass, codec);
        if (previous != null)
            throw new Error("entity or view class is registered, entityClass=" + entityClass.getCanonicalName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    CodecRegistry codecRegistry() {
        List<Codec<?>> codecs = new ArrayList<>(this.codecs.values());
        codecs.add(new LocalDateTimeCodec());
        codecs.add(new ZonedDateTimeCodec());
        codecs.add(new LocalDateCodec());
        for (Class<? extends Enum<?>> enumClass : enumClasses) {
            codecs.add(new EnumCodec(enumClass));
        }
        return CodecRegistries.fromCodecs(codecs);
    }
}
