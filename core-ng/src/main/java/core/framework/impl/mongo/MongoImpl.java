package core.framework.impl.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Mongo;
import core.framework.api.mongo.Query;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import org.bson.BsonDocument;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public final class MongoImpl implements Mongo, MongoOption {
    private final Logger logger = LoggerFactory.getLogger(MongoImpl.class);
    private final MongoClientOptions.Builder builder = MongoClientOptions.builder().socketKeepAlive(true);
    private final EntityCodecs codecs = new EntityCodecs();
    private final MongoEntityValidator validator = new MongoEntityValidator();

    private int tooManyRowsReturnedThreshold = 2000;
    private long slowOperationThresholdInMs = Duration.ofSeconds(5).toMillis();

    private MongoClientURI uri;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public void initialize() {
        try {
            if (uri == null) throw new Error("uri() must be called before initialize");
            builder.codecRegistry(codecRegistry());
            mongoClient = new MongoClient(uri);
            database = mongoClient.getDatabase(uri.getDatabase());
        } finally {
            logger.info("initialize mongo client, uri={}", uri);
        }
    }

    private CodecRegistry codecRegistry() {
        List<Codec<?>> codecs = new ArrayList<>(this.codecs.codecs.values());
        codecs.add(new LocalDateTimeCodec());
        return CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(codecs));
    }

    public void close() {
        logger.info("close mongodb client, uri={}", uri);
        mongoClient.close();
    }

    @Override
    public void uri(String uri) {
        this.uri = new MongoClientURI(uri, builder);
        if (this.uri.getDatabase() == null) throw Exceptions.error("uri must have database, uri={}", uri);
    }

    @Override
    public void poolSize(int minSize, int maxSize) {
        builder.minConnectionsPerHost(minSize)
            .connectionsPerHost(maxSize);
    }

    @Override
    public void timeout(Duration timeout) {
        builder.connectTimeout((int) timeout.toMillis()) // default is 10s
            .socketTimeout((int) timeout.toMillis());
    }

    @Override
    public <T> void entityClass(Class<T> entityClass) {
        new MongoClassValidator(entityClass).validateEntityClass();
        validator.register(entityClass);
        codecs.entityClass(entityClass);
    }

    @Override
    public <T> void viewClass(Class<T> viewClass) {
        new MongoClassValidator(viewClass).validateViewClass();
        codecs.viewClass(viewClass);
    }

    @Override
    public void setTooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {
        this.tooManyRowsReturnedThreshold = tooManyRowsReturnedThreshold;
    }

    @Override
    public void slowOperationThreshold(Duration threshold) {
        slowOperationThresholdInMs = threshold.toMillis();
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
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("insert, entityClass={}, elapsedTime={}", entity.getClass().getName(), elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public <T> Optional<T> get(Class<T> entityClass, Object id) {
        StopWatch watch = new StopWatch();
        try {
            T result = collection(entityClass).find(Filters.eq("_id", id)).first();
            return Optional.ofNullable(result);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("get, entityClass={}, id={}, elapsedTime={}", entityClass.getName(), id, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
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
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("findOne, entityClass={}, filter={}, elapsedTime={}", entityClass.getName(), filter, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public <T> List<T> find(Class<T> entityClass, Query query) {
        StopWatch watch = new StopWatch();
        List<T> results = Lists.newArrayList();
        try {
            FindIterable<T> collection = collection(entityClass)
                .find(query.filter == null ? new BsonDocument() : query.filter);
            if (query.projection != null) collection.projection(query.projection);
            if (query.sort != null) collection.sort(query.sort);
            if (query.skip != null) collection.skip(query.skip);
            if (query.limit != null) collection.limit(query.limit);

            for (T document : collection) {
                results.add(document);
            }
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("find, entityClass={}, filter={}, projection={}, sort={}, skip={}, limit={}, elapsedTime={}",
                entityClass.getName(),
                query.filter,
                query.projection,
                query.sort,
                query.skip,
                query.limit,
                elapsedTime);
            checkSlowOperation(elapsedTime);
            checkTooManyRowsReturned(results.size());
        }
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
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("aggregate, entityClass={}, pipeline={}, elapsedTime={}", entityClass.getName(), pipeline, elapsedTime);
            checkSlowOperation(elapsedTime);
            checkTooManyRowsReturned(results.size());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> long update(T entity) {
        StopWatch watch = new StopWatch();
        try {
            validator.validate(entity);
            Object id = codecs.id(entity);
            if (id == null) throw Exceptions.error("entity must have id to update, entityClass={}", entity.getClass().getCanonicalName());
            Bson filter = Filters.eq("_id", id);
            UpdateResult result = collection((Class<T>) entity.getClass()).replaceOne(filter, entity, new UpdateOptions().upsert(true));
            return result.getModifiedCount();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("update, entityClass={}, elapsedTime={}", entity.getClass().getName(), elapsedTime);
            checkSlowOperation(elapsedTime);
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
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("update, entityClass={}, filter={}, update={}, elapsedTime={}", entityClass, filter, update, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public <T> long delete(Class<T> entityClass, Object id) {
        StopWatch watch = new StopWatch();
        try {
            DeleteResult result = collection(entityClass).deleteOne(Filters.eq("_id", id));
            return result.getDeletedCount();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("delete, entityClass={}, id={}, elapsedTime={}", entityClass.getName(), id, elapsedTime);
            checkSlowOperation(elapsedTime);
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
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("delete, entityClass={}, filter={}, elapsedTime={}", entityClass, filter, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > slowOperationThresholdInMs) {
            logger.warn(Markers.errorCode("SLOW_MONGODB"), "slow mongoDB query, elapsedTime={}", elapsedTime);
        }
    }

    private void checkTooManyRowsReturned(int size) {
        if (size > tooManyRowsReturnedThreshold) {
            logger.warn(Markers.errorCode("TOO_MANY_ROWS_RETURNED"), "too many rows returned, returnedRows={}", size);
        }
    }

    private <T> MongoCollection<T> collection(Class<T> entityClass) {
        Collection collection = entityClass.getDeclaredAnnotation(Collection.class);
        if (database == null) initialize(); // lazy init for dev test, initialize will be called in startup hook for complete env
        return database.getCollection(collection.name(), entityClass);
    }
}
