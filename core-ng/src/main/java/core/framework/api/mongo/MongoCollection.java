package core.framework.api.mongo;

import org.bson.conversions.Bson;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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

    void forEach(Query query, Consumer<T> consumer);    // mongo driver fetches results in batch

    <V> List<V> aggregate(Class<V> resultClass, Bson... pipeline);

    void replace(T entity);

    long update(Bson filter, Bson update);

    long delete(Object id);

    long delete(Bson filter);
}
