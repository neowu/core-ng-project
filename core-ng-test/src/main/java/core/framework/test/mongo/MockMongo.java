package core.framework.test.mongo;

import com.mongodb.MongoClientURI;
import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Mongo;
import core.framework.api.mongo.Query;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.impl.mongo.EntityCodec;
import core.framework.impl.mongo.EntityCodecs;
import core.framework.impl.mongo.MongoClassValidator;
import core.framework.impl.mongo.MongoEntityValidator;
import core.framework.impl.mongo.MongoOption;
import org.bson.conversions.Bson;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class MockMongo implements Mongo, MongoOption {
    private final EntityCodecs codecs = new EntityCodecs();
    private final MongoEntityValidator validator = new MongoEntityValidator();
    private final Map<String, Map<Object, ?>> collections = Maps.newConcurrentHashMap();

    @Override
    public void uri(String uri) {
        if (new MongoClientURI(uri).getDatabase() == null)
            throw Exceptions.error("uri must have database, uri={}", uri);
    }

    @Override
    public void poolSize(int minSize, int maxSize) {

    }

    @Override
    public void timeout(Duration timeout) {

    }

    @Override
    public <T> void entityClass(Class<T> entityClass) {
        new MongoClassValidator(entityClass).validateEntityClass();
        validator.register(entityClass);
        codecs.entityClass(entityClass);
        collections.put(entityClass.getDeclaredAnnotation(Collection.class).name(), Maps.newConcurrentHashMap());
    }

    @Override
    public <T> void viewClass(Class<T> viewClass) {
        new MongoClassValidator(viewClass).validateViewClass();
        codecs.viewClass(viewClass);
    }

    @Override
    public void setTooManyRowsReturnedThreshold(int tooManyRowsReturnedThreshold) {

    }

    @Override
    public void slowOperationThreshold(Duration threshold) {

    }

    @Override
    public <T> void insert(T entity) {
        validator.validate(entity);
        @SuppressWarnings("unchecked")
        EntityCodec<T> codec = (EntityCodec<T>) codecs.codecs.get(entity.getClass());
        codec.generateIdIfAbsentFromDocument(entity);
        Map<Object, T> collection = collection(entity.getClass());
        Object id = codecs.id(entity);
        T previous = collection.putIfAbsent(id, entity);
        if (previous != null) throw Exceptions.error("entity with same id exists, id={}", id);
    }

    @Override
    public <T> Optional<T> get(Class<T> entityClass, Object id) {
        Map<Object, T> collection = collection(entityClass);
        return Optional.ofNullable(collection.get(id));
    }

    @Override
    public <T> List<T> find(Class<T> entityClass, Query query) {
        throw Exceptions.error("unsupported method, please use mock to test");
    }

    @Override
    public <T> long update(T entity) {
        validator.validate(entity);
        Map<Object, T> collection = collection(entity.getClass());
        Object id = codecs.id(entity);
        T previous = collection.put(id, entity);
        return previous != null ? 1 : 0;
    }

    @Override
    public <T> long delete(Class<T> entityClass, Object id) {
        Map<Object, T> collection = collection(entityClass);
        T removed = collection.remove(id);
        return removed != null ? 1 : 0;
    }

    @Override
    public <T> long delete(Class<T> entityClass, Bson filter) {
        throw Exceptions.error("unsupported method, please use mock to test");
    }

    @Override
    public <T> long update(Class<T> entityClass, Bson filter, Bson update) {
        throw Exceptions.error("unsupported method, please use mock to test");
    }

    @Override
    public <T, V> List<V> aggregate(Class<T> entityClass, Class<V> resultClass, Bson... pipeline) {
        throw Exceptions.error("unsupported method, please use mock to test");
    }

    @Override
    public <T> Optional<T> findOne(Class<T> entityClass, Bson filter) {
        throw Exceptions.error("unsupported method, please use mock to test");
    }

    @SuppressWarnings("unchecked")
    private <T> Map<Object, T> collection(Class<?> entityClass) {
        String name = entityClass.getDeclaredAnnotation(Collection.class).name();
        return (Map<Object, T>) collections.get(name);
    }
}
