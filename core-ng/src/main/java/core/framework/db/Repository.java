package core.framework.db;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

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

    default long count(String where, Object... params) {
        Query<T> query = select();
        if (where != null) query.where(where, params);
        return query.count();
    }

    default Optional<T> selectOne(String where, Object... params) {
        Query<T> query = select();
        if (where != null) query.where(where, params);
        return query.fetchOne();
    }

    Optional<T> get(Object... primaryKeys);

    OptionalLong insert(T entity);

    // refer to https://dev.mysql.com/doc/refman/8.0/en/insert.html
    // ignore if there is duplicated row, return true if insert successfully
    boolean insertIgnore(T entity);

    void update(T entity);

    void partialUpdate(T entity); // only update non-null fields

    void delete(Object... primaryKeys);

    void batchInsert(List<T> entities);

    void batchDelete(List<?> primaryKeys);
}
