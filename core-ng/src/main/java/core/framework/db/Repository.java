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
    // return true if the row actually changed
    boolean update(T entity);

    // only update non-null fields, return true if the row actually changed
    // if returned false, it CAN NOT distinguish if id not found, or all columns updated to its current values
    boolean partialUpdate(T entity);

    // partial update with additional condition, usually applied as optimistic lock pattern, return true if the row actually changed
    boolean partialUpdate(T entity, String where, Object... params);

    // return true if the row actually deleted
    boolean delete(Object... primaryKeys);

    Optional<long[]> batchInsert(List<T> entities);

    // return true if any row inserted
    // this is drawback of MySQL thin driver, though expected behavior
    // with batch insert ignore (or insert on duplicate key), MySQL thin driver fills entire affectedRows array with same value, java.sql.Statement.SUCCESS_NO_INFO if updated count > 0
    // refer to com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchWithMultiValuesClause Line 612
    boolean batchInsertIgnore(List<T> entities);

    // return true if any row changed (inserted or updated)
    // with batch, mysql treats both 1 (inserted) or 2 (updated) as java.sql.Statement.SUCCESS_NO_INFO
    // batch performance is significantly better than single call, try to do batch if possible on data sync
    boolean batchUpsert(List<T> entities);

    // return true if any row deleted
    // use Transaction if size of primaryKeys is too large, to avoid mysql create transaction for each statement
    // refer to com.mysql.cj.jdbc.ClientPreparedStatement.executePreparedBatchAsMultiStatement, mysql driver simply sends multiple queries with ';' as one statement
    boolean batchDelete(List<?> primaryKeys);
}
