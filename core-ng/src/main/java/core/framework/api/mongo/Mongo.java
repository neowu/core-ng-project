package core.framework.api.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import core.framework.impl.mongo.EntityIdHandler;
import core.framework.impl.mongo.MongoEntityValidator;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class Mongo {
    private final Logger logger = LoggerFactory.getLogger(Mongo.class);

    private final MongoClient mongoClient;

    private final MongoDatabase database;
    private final Map<Class, EntityIdHandler> idHandlers;
    private final long slowQueryThresholdInMs;
    private final int tooManyRowsReturnedThreshold;

    private final MongoEntityValidator validator;

    Mongo(MongoClient mongoClient,
          String databaseName,
          Map<Class, EntityIdHandler> idHandlers,
          MongoEntityValidator validator, int tooManyRowsReturnedThreshold, long slowQueryThresholdInMs) {
        this.mongoClient = mongoClient;
        database = mongoClient.getDatabase(databaseName);
        this.idHandlers = idHandlers;
        this.slowQueryThresholdInMs = slowQueryThresholdInMs;
        this.tooManyRowsReturnedThreshold = tooManyRowsReturnedThreshold;
        this.validator = validator;
    }

    public void shutdown() {
        logger.info("shutdown mongodb client, database={}", database.getName());
        mongoClient.close();
    }

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
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    public <T> Optional<T> findOne(Class<T> entityClass, String id) {
        return findOne(entityClass, Filters.eq("_id", new ObjectId(id)));
    }

    public <T> Optional<T> findOne(Class<T> entityClass, Bson filter) {
        StopWatch watch = new StopWatch();
        try {
            List<T> results = executeFind(Lists.newArrayList(), entityClass, filter);
            if (results.isEmpty()) return Optional.empty();
            if (results.size() > 1) throw new Error("more than one row returned, size=" + results.size());
            return Optional.of(results.get(0));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("findOne, entityClass={}, filter={}, elapsedTime={}", entityClass.getName(), filter, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    public <T> List<T> find(Class<T> entityClass, Bson filter) {
        StopWatch watch = new StopWatch();
        List<T> results = Lists.newArrayList();
        try {
            return executeFind(results, entityClass, filter);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("find, entityClass={}, filter={}, elapsedTime={}", entityClass.getName(), filter, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
            if (results.size() > tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

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
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
            if (results.size() > tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

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
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    public <T> long update(Class<T> entityClass, Bson filter, Bson update) {
        StopWatch watch = new StopWatch();
        try {
            UpdateResult result = collection(entityClass).updateMany(filter, update);
            return result.getModifiedCount();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("update, entityClass={}, filter={}, update={}, elapsedTime={}", entityClass, filter, update, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    public <T> void delete(Class<T> entityClass, ObjectId id) {
        StopWatch watch = new StopWatch();
        try {
            collection(entityClass).deleteOne(Filters.eq("_id", id));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("delete, entityClass={}, id={}, elapsedTime={}", entityClass.getName(), id, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    public <T> long delete(Class<T> entityClass, Bson filter) {
        StopWatch watch = new StopWatch();
        try {
            DeleteResult result = collection(entityClass).deleteMany(filter);
            return result.getDeletedCount();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongo", elapsedTime);
            logger.debug("delete, entityClass={}, filter={}, elapsedTime={}", entityClass, filter, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    private <T> List<T> executeFind(List<T> results, Class<T> entityClass, Bson filter) {
        FindIterable<T> documents = collection(entityClass).find(filter == null ? new BsonDocument() : filter);
        for (T document : documents) {
            results.add(document);
        }
        return results;
    }

    private <T> Bson idEqualsFilter(T entity) {
        @SuppressWarnings("unchecked")
        EntityIdHandler<T> idHandler = idHandlers.get(entity.getClass());
        return Filters.eq("_id", idHandler.get(entity));
    }

    private <T> MongoCollection<T> collection(Class<T> entityClass) {
        Collection collection = entityClass.getDeclaredAnnotation(Collection.class);
        return database.getCollection(collection.name(), entityClass);
    }
}
