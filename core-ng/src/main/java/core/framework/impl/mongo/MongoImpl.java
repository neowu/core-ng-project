package core.framework.impl.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Mongo;
import core.framework.api.mongo.MongoCollection;
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
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class MongoImpl implements Mongo {
    final EntityCodecs codecs = new EntityCodecs();
    final MongoEntityValidator validator = new MongoEntityValidator();
    private final Logger logger = LoggerFactory.getLogger(MongoImpl.class);
    private final MongoClientOptions.Builder builder = MongoClientOptions.builder().socketKeepAlive(true);
    int timeoutInMs = (int) Duration.ofSeconds(10).toMillis();
    int tooManyRowsReturnedThreshold = 2000;
    long slowOperationThresholdInMs = Duration.ofSeconds(5).toMillis();

    private MongoClientURI uri;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public void initialize() {
        StopWatch watch = new StopWatch();
        try {
            if (uri == null) throw new Error("uri() must be called before initialize");
            database = createDatabase(uri, codecRegistry());
        } finally {
            logger.info("initialize mongo client, uri={}, elapsedTime={}", uri, watch.elapsedTime());
        }
    }

    protected MongoDatabase createDatabase(MongoClientURI uri, CodecRegistry registry) {
        builder.connectTimeout(timeoutInMs);
        builder.socketTimeout(timeoutInMs);
        builder.codecRegistry(registry);
        mongoClient = new MongoClient(uri);
        return mongoClient.getDatabase(uri.getDatabase());
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

    public void uri(String uri) {
        this.uri = new MongoClientURI(uri, builder);
        if (this.uri.getDatabase() == null) throw Exceptions.error("uri must have database, uri={}", uri);
    }

    public void poolSize(int minSize, int maxSize) {
        builder.minConnectionsPerHost(minSize);
        builder.connectionsPerHost(maxSize);
    }

    public final void timeout(Duration timeout) {
        timeoutInMs = (int) timeout.toMillis();
    }

    public <T> MongoCollection<T> collection(Class<T> entityClass) {
        new MongoClassValidator(entityClass).validateEntityClass();
        validator.register(entityClass);
        codecs.entityClass(entityClass);
        return new MongoCollectionImpl<>(this, entityClass);
    }

    public <T> void viewClass(Class<T> viewClass) {
        new MongoClassValidator(viewClass).validateViewClass();
        codecs.viewClass(viewClass);
    }

    public void tooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {
        this.tooManyRowsReturnedThreshold = tooManyRowsReturnedThreshold;
    }

    public void slowOperationThreshold(Duration threshold) {
        slowOperationThresholdInMs = threshold.toMillis();
    }

    @Override
    public <T> void insert(T entity) {
        StopWatch watch = new StopWatch();
        try {
            validator.validate(entity);
            @SuppressWarnings("unchecked")
            com.mongodb.client.MongoCollection<T> collection = mongoCollection((Class<T>) entity.getClass());
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
            T result = mongoCollection(entityClass).find(Filters.eq("_id", id)).first();
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
            FindIterable<T> query = mongoCollection(entityClass)
                .find(filter == null ? new BsonDocument() : filter)
                .maxTime(timeoutInMs, TimeUnit.MILLISECONDS)
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
            FindIterable<T> collection = mongoCollection(entityClass)
                .find(query.filter == null ? new BsonDocument() : query.filter)
                .maxTime(timeoutInMs, TimeUnit.MILLISECONDS);
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
            AggregateIterable<V> documents = mongoCollection(entityClass).aggregate(Lists.newArrayList(pipeline), resultClass)
                .maxTime(timeoutInMs, TimeUnit.MILLISECONDS);
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
            UpdateResult result = mongoCollection((Class<T>) entity.getClass()).replaceOne(filter, entity, new UpdateOptions().upsert(true));
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
            UpdateResult result = mongoCollection(entityClass).updateMany(filter, update);
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
            DeleteResult result = mongoCollection(entityClass).deleteOne(Filters.eq("_id", id));
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
            DeleteResult result = mongoCollection(entityClass).deleteMany(filter);
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

    <T> com.mongodb.client.MongoCollection<T> mongoCollection(Class<T> entityClass) {
        Collection collection = entityClass.getDeclaredAnnotation(Collection.class);
        if (database == null) initialize(); // lazy init for dev test, initialize will be called in startup hook for complete env
        return database.getCollection(collection.name(), entityClass);
    }
}
