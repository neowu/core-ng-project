package core.framework.api.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Repository<T> {
    Query<T> select();

    default List<T> select(String where, Object... params) {
        return select().where(where, params).fetch();
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
