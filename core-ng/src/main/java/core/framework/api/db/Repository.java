package core.framework.api.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Repository<T> {
    List<T> select(Query query);

    default List<T> select(String where, Object... params) {
        Query query = new Query();
        query.where = where;
        query.params = params;
        return select(query);
    }

    Optional<T> selectOne(String where, Object... params);

    Optional<T> get(Object... primaryKeys);

    Optional<Long> insert(T entity);

    void update(T entity);

    void delete(Object... primaryKeys);

    void batchInsert(List<T> entities);

    void batchDelete(List<?> primaryKeys);
}
