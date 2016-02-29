package core.framework.impl.mongo;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
import core.framework.api.mongo.Collection;
import core.framework.api.mongo.MongoCollection;
import core.framework.api.mongo.Query;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class MongoCollectionImpl<T> implements MongoCollection<T> {
    private final Logger logger = LoggerFactory.getLogger(MongoCollectionImpl.class);
    private final Mongo mongo;
    private final Class<T> entityClass;
    private final String collectionName;
    private final EntityValidator<T> validator;
    private com.mongodb.client.MongoCollection<T> collection;

    public MongoCollectionImpl(Mongo mongo, Class<T> entityClass) {
        this.mongo = mongo;
        this.entityClass = entityClass;
        validator = new EntityValidator<>(entityClass);
        collectionName = entityClass.getDeclaredAnnotation(Collection.class).name();
    }

    @Override
    public long count(Bson filter) {
        StopWatch watch = new StopWatch();
        try {
            return collection().count(filter, new CountOptions().maxTime(mongo.timeoutInMs, TimeUnit.MILLISECONDS));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("count, collection={}, filter={}, elapsedTime={}", collectionName, filter, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void insert(T entity) {
        StopWatch watch = new StopWatch();
        validator.validate(entity);
        try {
            collection().insertOne(entity);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("insert, collection={}, elapsedTime={}", collectionName, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Optional<T> get(Object id) {
        StopWatch watch = new StopWatch();
        try {
            T result = collection().find(Filters.eq("_id", id)).first();
            return Optional.ofNullable(result);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("get, collection={}, id={}, elapsedTime={}", collectionName, id, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Optional<T> findOne(Bson filter) {
        StopWatch watch = new StopWatch();
        List<T> results = Lists.newArrayList();
        try {
            FindIterable<T> cursor = collection()
                .find(filter == null ? new BsonDocument() : filter)
                .maxTime(mongo.timeoutInMs, TimeUnit.MILLISECONDS)
                .limit(2);
            cursor.into(results);
            if (results.isEmpty()) return Optional.empty();
            if (results.size() > 1) throw Exceptions.error("more than one row returned, size={}", results.size());
            return Optional.of(results.get(0));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("findOne, collection={}, filter={}, elapsedTime={}", collectionName, filter, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public List<T> find(Query query) {
        StopWatch watch = new StopWatch();
        List<T> results = Lists.newArrayList();
        try {
            FindIterable<T> cursor = collection()
                .find(query.filter == null ? new BsonDocument() : query.filter)
                .maxTime(mongo.timeoutInMs, TimeUnit.MILLISECONDS);
            if (query.projection != null) cursor.projection(query.projection);
            if (query.sort != null) cursor.sort(query.sort);
            if (query.skip != null) cursor.skip(query.skip);
            if (query.limit != null) cursor.limit(query.limit);
            cursor.into(results);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("find, collection={}, filter={}, projection={}, sort={}, skip={}, limit={}, elapsedTime={}",
                collectionName,
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
    public <V> List<V> aggregate(Class<V> resultClass, Bson... pipeline) {
        StopWatch watch = new StopWatch();
        List<V> results = Lists.newArrayList();
        try {
            AggregateIterable<V> cursor = collection().aggregate(Lists.newArrayList(pipeline), resultClass)
                .maxTime(mongo.timeoutInMs, TimeUnit.MILLISECONDS);
            cursor.into(results);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("aggregate, collection={}, pipeline={}, elapsedTime={}", collectionName, pipeline, elapsedTime);
            checkSlowOperation(elapsedTime);
            checkTooManyRowsReturned(results.size());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void replace(T entity) {
        StopWatch watch = new StopWatch();
        Object id = null;
        validator.validate(entity);
        try {
            id = mongo.codecs.id(entity);
            if (id == null) throw Exceptions.error("entity must have id, entityClass={}", entityClass.getCanonicalName());
            collection().replaceOne(Filters.eq("_id", id), entity, new UpdateOptions().upsert(true));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("replace, collection={}, id={}, elapsedTime={}", collectionName, id, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public long update(Bson filter, Bson update) {
        StopWatch watch = new StopWatch();
        try {
            UpdateResult result = collection().updateMany(filter, update);
            return result.getModifiedCount();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("update, collection={}, filter={}, update={}, elapsedTime={}", collectionName, filter, update, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public long delete(Object id) {
        StopWatch watch = new StopWatch();
        try {
            DeleteResult result = collection().deleteOne(Filters.eq("_id", id));
            return result.getDeletedCount();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("delete, collection={}, id={}, elapsedTime={}", collectionName, id, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public long delete(Bson filter) {
        StopWatch watch = new StopWatch();
        try {
            DeleteResult result = collection().deleteMany(filter);
            return result.getDeletedCount();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("mongoDB", elapsedTime);
            logger.debug("delete, collection={}, filter={}, elapsedTime={}", collectionName, filter, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > mongo.slowOperationThresholdInMs) {
            logger.warn(Markers.errorCode("SLOW_MONGODB"), "slow mongoDB query, elapsedTime={}", elapsedTime);
        }
    }

    private void checkTooManyRowsReturned(int size) {
        if (size > mongo.tooManyRowsReturnedThreshold) {
            logger.warn(Markers.errorCode("TOO_MANY_ROWS_RETURNED"), "too many rows returned, returnedRows={}", size);
        }
    }

    private com.mongodb.client.MongoCollection<T> collection() {
        if (collection == null) {
            collection = mongo.mongoCollection(entityClass);
        }
        return collection;
    }
}
