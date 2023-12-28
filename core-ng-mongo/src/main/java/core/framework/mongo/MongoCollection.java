package core.framework.mongo;

import org.bson.conversions.Bson;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author neo
 */
public interface MongoCollection<T> {
    long count(Count count);

    default long count(Bson filter) {
        var count = new Count();
        count.filter = filter;
        return count(count);
    }

    void insert(T entity);

    void bulkInsert(List<T> entities);

    Optional<T> get(Get get);

    default Optional<T> get(Object id) {
        var get = new Get();
        get.id = id;
        return get(get);
    }

    Optional<T> findOne(FindOne findOne);

    default Optional<T> findOne(Bson filter) {
        var findOne = new FindOne();
        findOne.filter = filter;
        return findOne(findOne);
    }

    List<T> find(Query query);

    default List<T> find(Bson filter) {
        var query = new Query();
        query.filter = filter;
        return find(query);
    }

    void forEach(Query query, Consumer<T> consumer);    // mongo driver fetches results in batch

    <V> List<V> aggregate(Aggregate<V> aggregate);

    void replace(T entity);

    void bulkReplace(List<T> entities);

    long update(Bson filter, Bson update);

    boolean delete(Object id);

    long delete(Bson filter);

    long bulkDelete(List<?> ids);
}
