package core.framework.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Repository<T> {
    Query<T> select();

    default List<T> select(String where, Object... params) {
        Query<T> query = select();
        if (where != null) query.where(where, params);
        return query.fetch();
    }

    int count(String where, Object... params);

    Optional<T> selectOne(String where, Object... params);

    Optional<T> get(Object... primaryKeys);

    Optional<Long> insert(T entity);

    void update(T entity);

    void delete(Object... primaryKeys);

    void batchInsert(List<T> entities);

    void batchDelete(List<?> primaryKeys);
}
