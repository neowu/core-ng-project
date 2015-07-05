package core.framework.api.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Repository<T> {
    List<T> select(String whereClause, Object... params);

    Optional<T> selectOne(String whereClause, Object... params);

    Optional<T> get(Object... primaryKeys);

    Optional<Long> insert(T entity);

    void update(T entity);

    void delete(Object... primaryKeys);

    void batchInsert(List<T> entities);

    void batchDelete(List<?> primaryKeys);
}
