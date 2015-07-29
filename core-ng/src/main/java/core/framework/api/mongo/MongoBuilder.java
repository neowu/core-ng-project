package core.framework.api.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author neo
 */
public class MongoBuilder implements Supplier<Mongo> {
    private final Logger logger = LoggerFactory.getLogger(MongoBuilder.class);

    private final MongoClientOptions.Builder builder = MongoClientOptions.builder();
    private String uri;
    private Duration slowQueryThreshold = Duration.ofSeconds(5);
    private int tooManyRowsReturnedThreshold = 2000;

    private final Map<Class<?>, Codec<?>> codecs = Maps.newHashMap();
    private final Map<Class, EntityIdHandler> idHandlers = Maps.newHashMap();
    private final MongoEntityValidator validator = new MongoEntityValidator();

    public MongoBuilder uri(String uri) {
        this.uri = uri;
        return this;
    }

    public MongoBuilder poolSize(int minSize, int maxSize) {
        builder.minConnectionsPerHost(minSize)
            .connectionsPerHost(maxSize);
        return this;
    }

    public MongoBuilder timeout(Duration timeout) {
        builder.connectTimeout((int) timeout.toMillis())
            .socketTimeout((int) timeout.toMillis());
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
        validator.register(entityClass);
        EntityIdHandler<T> entityIdHandler = new EntityIdHandlerBuilder<>(entityClass).build();
        idHandlers.put(entityClass, entityIdHandler);
        registerCodec(entityClass, entityIdHandler);
        return this;
    }

    public <T> MongoBuilder viewClass(Class<T> viewClass) {
        new MongoClassValidator(viewClass).validateViewClass();
        registerCodec(viewClass, null);
        return this;
    }

    private <T> void registerCodec(Class<T> entityClass, EntityIdHandler<T> entityIdHandler) {
        EntityEncoder<T> entityEncoder = new EntityEncoderBuilder<>(entityClass).build();
        EntityDecoder<T> entityDecoder = new EntityDecoderBuilder<>(entityClass).build();
        EntityCodec<T> codec = new EntityCodec<>(entityClass, entityIdHandler, entityEncoder, entityDecoder);
        Codec<?> previous = codecs.putIfAbsent(entityClass, codec);
        if (previous != null)
            throw Exceptions.error("entity or view class is registered, class={}", entityClass.getCanonicalName());
    }

    protected Mongo createMongo(MongoClientURI uri) {
        logger.info("create mongodb client, uri={}", uri);
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase(uri.getDatabase());
        return new Mongo(mongoClient, database);
    }

    @Override
    public Mongo get() {
        builder.socketKeepAlive(true);

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
            CodecRegistries.fromCodecs(new ArrayList<>(codecs.values())));
        builder.codecRegistry(codecRegistry);

        MongoClientURI mongoURI = new MongoClientURI(uri, builder);
        if (mongoURI.getDatabase() == null) throw Exceptions.error("database must present in mongo uri, uri={}", uri);

        Mongo mongo = createMongo(mongoURI);

        mongo.idHandlers = idHandlers;
        mongo.slowQueryThresholdInMs = slowQueryThreshold.toMillis();
        mongo.tooManyRowsReturnedThreshold = tooManyRowsReturnedThreshold;
        mongo.validator = validator;

        return mongo;
    }
}
