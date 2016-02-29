package core.framework.api.mongo;

import org.bson.conversions.Bson;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
@Deprecated
public interface Mongo {
    <T> void insert(T entity);

    <T> Optional<T> get(Class<T> entityClass, Object id);

    <T> Optional<T> findOne(Class<T> entityClass, Bson filter);

    <T> List<T> find(Class<T> entityClass, Query query);

    default <T> List<T> find(Class<T> entityClass, Bson filter) {
        Query query = new Query();
        query.filter = filter;
        return find(entityClass, query);
    }

    <T, V> List<V> aggregate(Class<T> entityClass, Class<V> resultClass, Bson... pipeline);

    <T> long update(T entity);

    <T> long update(Class<T> entityClass, Bson filter, Bson update);

    <T> long delete(Class<T> entityClass, Object id);

    <T> long delete(Class<T> entityClass, Bson filter);
}
