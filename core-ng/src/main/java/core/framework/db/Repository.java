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
    // BE CAUTION, it uses PK or unique index to determine duplication !!! read mysql doc carefully to avoid unexpected side effect
    // BE CAUTION, With IGNORE, invalid values are adjusted to the closest values and inserted; warnings are produced but the statement does not abort.
    // e.g. if you pass to out of range timestamp value, insert ignore will save it as 0 in db, which is terrible design of MySQL
    boolean insertIgnore(T entity);

    // refer to https://dev.mysql.com/doc/refman/8.0/en/insert-on-duplicate.html
    // use insert on duplicate key sql, generally used by data sync
    // BE CAUTION, it uses PK or unique index to determine duplication !!! read mysql doc carefully to avoid unexpected side effect
    // return true if the new row inserted
    boolean upsert(T entity);

    // use update carefully, it will update all the columns according to the entity fields, includes null fields
    // generally it's recommended to use partialUpdate if only few columns need to be updated and with optimistic lock
    boolean update(T entity);

    // only update non-null fields, return true if the row actually changed
    // if returned false, it CAN NOT distinguish if id not found, or all columns updated to its current values
    boolean partialUpdate(T entity);

    // partial update with additional condition, usually applied as optimistic lock pattern, return true if the row actually changed
    boolean partialUpdate(T entity, String where, Object... params);

    // return true if the row actually deleted
    boolean delete(Object... primaryKeys);

    Optional<long[]> batchInsert(List<T> entities);

    // return whether each inserted successfully
    boolean[] batchInsertIgnore(List<T> entities);

    // batch performance is significantly better than single call, try to do batch if possible on data sync
    // return true if the new row inserted
    boolean[] batchUpsert(List<T> entities);

    // use Transaction if size of primaryKeys is too large, to avoid mysql create transaction for each statement
    // refer to com.mysql.cj.jdbc.ClientPreparedStatement.executePreparedBatchAsMultiStatement, mysql driver simply sends multiple queries with ';' as one statement
    boolean[] batchDelete(List<?> primaryKeys);
}
