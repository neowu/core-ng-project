package core.framework.impl.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import core.framework.api.log.ActionLogContext;
import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Mongo;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import org.bson.BsonDocument;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class MongoImpl implements Mongo {
    private final Logger logger = LoggerFactory.getLogger(MongoImpl.class);
    private final MongoClientOptions.Builder builder = MongoClientOptions.builder().socketKeepAlive(true);
    private final Map<Class<?>, Codec<?>> codecs = Maps.newHashMap();
    private final Map<Class, EntityIdHandler> idHandlers = Maps.newHashMap();
    private final MongoEntityValidator validator = new MongoEntityValidator();
    public MongoClient mongoClient;
    public int tooManyRowsReturnedThreshold = 2000;
    private MongoDatabase database;
    private long slowQueryThresholdInMs = Duration.ofSeconds(5).toMillis();
    private MongoClientURI uri;

    public void uri(String uri) {
        logger.info("set mongo uri, uri={}", uri);
        this.uri = new MongoClientURI(uri, builder);
        if (this.uri.getDatabase() == null) throw Exceptions.error("uri must have database, uri={}", uri);
    }

    public void poolSize(int minSize, int maxSize) {
        builder.minConnectionsPerHost(minSize)
            .connectionsPerHost(maxSize);
    }

    public void timeout(Duration timeout) {
        builder.connectTimeout((int) timeout.toMillis()) // default is 10s
            .socketTimeout((int) timeout.toMillis());
    }

    public void close() {
        if (mongoClient != null) {
            logger.info("close mongodb client, database={}", database.getName());
            mongoClient.close();
        }
    }

    public <T> void entityClass(Class<T> entityClass) {
        validator.register(entityClass);
        EntityIdHandler<T> entityIdHandler = new EntityIdHandlerBuilder<>(entityClass).build();
        idHandlers.put(entityClass, entityIdHandler);
        registerCodec(entityClass, entityIdHandler);
    }

    public <T> void viewClass(Class<T> viewClass) {
        new MongoClassValidator(viewClass).validateViewClass();
        registerCodec(viewClass, null);
    }

    private <T> void registerCodec(Class<T> entityClass, EntityIdHandler<T> entityIdHandler) {
        EntityEncoder<T> entityEncoder = new EntityEncoderBuilder<>(entityClass).build();
        EntityDecoder<T> entityDecoder = new EntityDecoderBuilder<>(entityClass).build();
        EntityCodec<T> codec = new EntityCodec<>(entityClass, entityIdHandler, entityEncoder, entityDecoder);
        Codec<?> previous = codecs.putIfAbsent(entityClass, codec);
        if (previous != null)
            throw Exceptions.error("entity or view class is registered, class={}", entityClass.getCanonicalName());
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        slowQueryThresholdInMs = slowQueryThreshold.toMillis();
    }

    private MongoDatabase database() {
        if (mongoClient == null) {
            if (uri == null) throw new Error("uri() must be called before initialize");
            mongoClient = new MongoClient(uri);
        }

        if (database == null) {
            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(new ArrayList<>(codecs.values())));
            database = mongoClient.getDatabase(uri.getDatabase()).withCodecRegistry(codecRegistry);
        }

        return database;
    }

    @Override
    public <T> void insert(T entity) {
        StopWatch watch = new StopWatch();
        try {
            validator.validate(entity);
            @SuppressWarnings("unchecked")
            MongoCollection<T> collection = collection((Class<T>) entity.getClass());
            collection.insertOne(entity);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("insert, entityClass={}, elapsedTime={}", entity.getClass().getName(), elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public <T> Optional<T> findOne(Class<T> entityClass, ObjectId id) {
        return findOne(entityClass, Filters.eq("_id", id));
    }

    @Override
    public <T> Optional<T> findOne(Class<T> entityClass, Bson filter) {
        StopWatch watch = new StopWatch();
        try {
            FindIterable<T> query = collection(entityClass)
                .find(filter == null ? new BsonDocument() : filter)
                .limit(2);
            List<T> results = Lists.newArrayList();
            for (T document : query) {
                results.add(document);
            }
            if (results.isEmpty()) return Optional.empty();
            if (results.size() > 1) throw Exceptions.error("more than one row returned, size={}", results.size());
            return Optional.of(results.get(0));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("findOne, entityClass={}, filter={}, elapsedTime={}", entityClass.getName(), filter, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public <T> List<T> find(Class<T> entityClass, Bson filter, Bson sort, Integer skip, Integer limit) {
        StopWatch watch = new StopWatch();
        List<T> results = Lists.newArrayList();
        try {
            FindIterable<T> query = collection(entityClass)
                .find(filter == null ? new BsonDocument() : filter);

            if (sort != null) query.sort(sort);
            if (skip != null) query.skip(skip);
            if (limit != null) query.limit(limit);

            for (T document : query) {
                results.add(document);
            }
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("find, entityClass={}, filter={}, sort={}, skip={}, limit={}, elapsedTime={}", entityClass.getName(), filter, sort, skip, limit, elapsedTime);
            checkSlowQuery(elapsedTime);
            if (results.size() > tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

    @Override
    public <T> List<T> find(Class<T> entityClass, Bson filter) {
        return find(entityClass, filter, null, null, null);
    }

    @Override
    public <T, V> List<V> aggregate(Class<T> entityClass, Class<V> resultClass, Bson... pipeline) {
        StopWatch watch = new StopWatch();
        List<V> results = Lists.newArrayList();
        try {
            MongoCollection<T> collection = collection(entityClass);
            AggregateIterable<V> documents = collection.aggregate(Lists.newArrayList(pipeline), resultClass);
            for (V document : documents) {
                results.add(document);
            }
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("aggregate, entityClass={}, pipeline={}, elapsedTime={}", entityClass.getName(), pipeline, elapsedTime);
            checkSlowQuery(elapsedTime);
            if (results.size() > tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

    @Override
    public <T> void update(T entity) {
        StopWatch watch = new StopWatch();
        try {
            validator.validate(entity);
            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) entity.getClass();
            Bson filter = idEqualsFilter(entity);
            collection(entityClass).replaceOne(filter, entity);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("update, entityClass={}, elapsedTime={}", entity.getClass().getName(), elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public <T> long update(Class<T> entityClass, Bson filter, Bson update) {
        StopWatch watch = new StopWatch();
        try {
            UpdateResult result = collection(entityClass).updateMany(filter, update);
            return result.getModifiedCount();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("update, entityClass={}, filter={}, update={}, elapsedTime={}", entityClass, filter, update, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public <T> void delete(Class<T> entityClass, ObjectId id) {
        StopWatch watch = new StopWatch();
        try {
            collection(entityClass).deleteOne(Filters.eq("_id", id));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("delete, entityClass={}, id={}, elapsedTime={}", entityClass.getName(), id, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
    public <T> long delete(Class<T> entityClass, Bson filter) {
        StopWatch watch = new StopWatch();
        try {
            DeleteResult result = collection(entityClass).deleteMany(filter);
            return result.getDeletedCount();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("delete, entityClass={}, filter={}, elapsedTime={}", entityClass, filter, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    private <T> Bson idEqualsFilter(T entity) {
        @SuppressWarnings("unchecked")
        EntityIdHandler<T> idHandler = idHandlers.get(entity.getClass());
        return Filters.eq("_id", idHandler.get(entity));
    }

    private void checkSlowQuery(long elapsedTime) {
        if (elapsedTime > slowQueryThresholdInMs)
            logger.warn("slow query detected");
    }

    private <T> MongoCollection<T> collection(Class<T> entityClass) {
        Collection collection = entityClass.getDeclaredAnnotation(Collection.class);
        return database().getCollection(collection.name(), entityClass);
    }
}
