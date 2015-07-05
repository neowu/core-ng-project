package core.framework.api.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.Sets;
import core.framework.impl.mongo.EntityCodec;
import core.framework.impl.mongo.EntityDecoder;
import core.framework.impl.mongo.EntityDecoderBuilder;
import core.framework.impl.mongo.EntityEncoder;
import core.framework.impl.mongo.EntityEncoderBuilder;
import core.framework.impl.mongo.EntityIdHandler;
import core.framework.impl.mongo.EntityIdHandlerBuilder;
import core.framework.impl.mongo.MongoClassValidator;
import core.framework.impl.mongo.MongoEntityValidator;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author neo
 */
public final class MongoBuilder implements Supplier<Mongo> {
    private final Logger logger = LoggerFactory.getLogger(MongoBuilder.class);

    private final MongoClientOptions.Builder builder = MongoClientOptions.builder();
    private String uri;
    private String databaseName;
    private Duration slowQueryThreshold = Duration.ofSeconds(5);
    private int tooManyRowsReturnedThreshold = 2000;

    private final Set<Class> entityClasses = Sets.newHashSet();

    private final List<Codec<?>> entityCodecs = Lists.newArrayList();
    private final Map<Class, EntityIdHandler> idHandlers = Maps.newHashMap();
    private final MongoEntityValidator validator = new MongoEntityValidator();

    public MongoBuilder uri(String uri) {
        this.uri = uri;
        return this;
    }

    public MongoBuilder databaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public MongoBuilder poolSize(int minSize, int maxSize) {
        builder.minConnectionsPerHost(minSize)
            .connectionsPerHost(maxSize);
        return this;
    }

    public MongoBuilder slowQueryThreshold(Duration slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
        return this;
    }

    public MongoBuilder tooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {
        this.tooManyRowsReturnedThreshold = tooManyRowsReturnedThreshold;
        return this;
    }

    public <T> MongoBuilder entityClass(Class<T> entityClass) {
        checkIfRegistered(entityClass);

        validator.register(entityClass);

        EntityIdHandler<T> entityIdHandler = new EntityIdHandlerBuilder<>(entityClass).build();
        idHandlers.put(entityClass, entityIdHandler);

        registerCodec(entityClass, entityIdHandler);
        return this;
    }

    public <T> MongoBuilder viewClass(Class<T> viewClass) {
        checkIfRegistered(viewClass);

        new MongoClassValidator(viewClass).validateViewClass();

        registerCodec(viewClass, null);
        return this;
    }

    private <T> void checkIfRegistered(Class<T> entityClass) {
        if (entityClasses.contains(entityClass))
            throw Exceptions.error("entity or view class is registered, class={}", entityClass.getCanonicalName());

        entityClasses.add(entityClass);
    }

    private <T> void registerCodec(Class<T> entityClass, EntityIdHandler<T> entityIdHandler) {
        EntityEncoder<T> entityEncoder = new EntityEncoderBuilder<>(entityClass).build();
        EntityDecoder<T> entityDecoder = new EntityDecoderBuilder<>(entityClass).build();
        EntityCodec<T> codec = new EntityCodec<>(entityClass, entityIdHandler, entityEncoder, entityDecoder);
        entityCodecs.add(codec);
    }

    @Override
    public Mongo get() {
        logger.info("create mongodb client, uri={}", uri);
        builder.socketKeepAlive(true);

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
            CodecRegistries.fromCodecs(entityCodecs));
        builder.codecRegistry(codecRegistry);

        MongoClient mongoClient = new MongoClient(new MongoClientURI(uri, builder));
        return new Mongo(mongoClient, databaseName, idHandlers, validator, tooManyRowsReturnedThreshold, slowQueryThreshold.toMillis());
    }
}
