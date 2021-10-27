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
    // ignore if there is duplicate row, return true if inserted successfully
    boolean insertIgnore(T entity);

    // refer to https://dev.mysql.com/doc/refman/8.0/en/insert-on-duplicate.html
    // use insert on duplicate key sql, generally used by data sync
    // BE CAUTION, it uses PK or unique index to determine duplication !!! read mysql doc carefully to avoid unexpected side effect
    void upsert(T entity);

    // use update carefully, it will update all the columns according to the entity fields, includes null fields
    // generally it's recommended to use partialUpdate if only few columns need to be updated and with optimistic lock
    boolean update(T entity);

    // only update non-null fields
    boolean partialUpdate(T entity);

    // partial update with additional condition, usually applied as optimistic lock pattern, return true if updated successfully
    boolean partialUpdate(T entity, String where, Object... params);

    boolean delete(Object... primaryKeys);

    Optional<long[]> batchInsert(List<T> entities);

    // return whether each inserted successfully
    boolean[] batchInsertIgnore(List<T> entities);

    // batch performance is significantly better than single call, try to do batch if possible on data sync
    void batchUpsert(List<T> entities);

    boolean[] batchDelete(List<?> primaryKeys);
}
