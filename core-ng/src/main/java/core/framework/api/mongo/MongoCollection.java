package core.framework.api.mongo;

import org.bson.conversions.Bson;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface MongoCollection<T> {
    long count(Bson filter);

    void insert(T entity);

    Optional<T> get(Object id);

    Optional<T> findOne(Bson filter);

    List<T> find(Query query);

    default List<T> find(Bson filter) {
        Query query = new Query();
        query.filter = filter;
        return find(query);
    }

    <V> List<V> aggregate(Class<V> resultClass, Bson... pipeline);

    void replace(T entity);

    long update(Bson filter, Bson update);

    long delete(Object id);

    long delete(Bson filter);
}
