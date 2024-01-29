package core.framework.internal.db;

import core.framework.db.Query;
import core.framework.db.Repository;
import core.framework.internal.validate.Validator;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class RepositoryImpl<T> implements Repository<T> {
    private final Logger logger = LoggerFactory.getLogger(RepositoryImpl.class);
    private final DatabaseImpl database;
    private final Validator<T> validator;
    private final SelectQuery<T> selectQuery;
    private final InsertQuery<T> insertQuery;
    private final UpdateQuery<T> updateQuery;
    private final String deleteSQL;
    private final Class<T> entityClass;

    RepositoryImpl(DatabaseImpl database, Class<T> entityClass, Dialect dialect) {
        this.database = database;
        validator = Validator.of(entityClass);
        insertQuery = new InsertQueryBuilder<>(entityClass, dialect).build();
        selectQuery = new SelectQuery<>(entityClass, dialect);
        updateQuery = new UpdateQueryBuilder<>(entityClass).build();
        deleteSQL = DeleteQueryBuilder.build(entityClass);
        this.entityClass = entityClass;
    }

    @Override
    public Query<T> select() {
        return new QueryImpl<>(database, entityClass, selectQuery);
    }

    @Override
    public Optional<T> get(Object... primaryKeys) {
        if (primaryKeys.length != selectQuery.primaryKeyColumns)
            throw new Error(Strings.format("the length of primary keys does not match columns, primaryKeys={}, columns={}", selectQuery.primaryKeyColumns, primaryKeys.length));
        return database.selectOne(selectQuery.getSQL, entityClass, primaryKeys);
    }

    @Override
    public OptionalLong insert(T entity) {
        var watch = new StopWatch();
        validator.validate(entity, false);
        String sql = insertQuery.insertSQL;
        Object[] params = insertQuery.params(entity);
        try {
            return database.operation.insert(sql, params, insertQuery.generatedColumn);
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("insert, sql={}, params={}, elapsed={}", sql, new SQLParams(database.operation.enumMapper, params), elapsed);
            database.track(elapsed, 0, 1, 1);
        }
    }

    @Override
    public boolean insertIgnore(T entity) {
        var watch = new StopWatch();
        if (insertQuery.generatedColumn != null) throw new Error("entity must not have auto increment primary key, entityClass=" + entityClass.getCanonicalName());
        validator.validate(entity, false);
        int insertedRows = 0;
        String sql = insertQuery.insertIgnoreSQL;
        Object[] params = insertQuery.params(entity);
        try {
            insertedRows = database.operation.update(sql, params);
            return insertedRows == 1;
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("insertIgnore, sql={}, params={}, inserted={}, elapsed={}", sql, new SQLParams(database.operation.enumMapper, params), insertedRows == 1, elapsed);
            database.track(elapsed, 0, insertedRows, 1);
        }
    }

    @Override
    public boolean upsert(T entity) {
        var watch = new StopWatch();
        if (insertQuery.generatedColumn != null) throw new Error("entity must not have auto increment primary key, entityClass=" + entityClass.getCanonicalName());
        validator.validate(entity, false);
        int affectedRows = 0;
        String sql = insertQuery.upsertSQL;
        Object[] params = insertQuery.params(entity);
        try {
            affectedRows = database.operation.update(sql, params);
            // refer to https://dev.mysql.com/doc/refman/8.0/en/insert-on-duplicate.html
            // With ON DUPLICATE KEY UPDATE, the affected-rows value per row is 1 if the row is inserted as a new row, 2 if an existing row is updated, and 0 if an existing row is set to its current values.
            return affectedRows == 1;
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("upsert, sql={}, params={}, inserted={}, elapsed={}", sql, new SQLParams(database.operation.enumMapper, params), affectedRows == 1, elapsed);
            database.track(elapsed, 0, affectedRows == 0 ? 0 : 1, 1);
        }
    }

    @Override
    public boolean update(T entity) {
        // return true if any column changed, false if no column changed or id not found
        return update(entity, false, null, null);
    }

    private boolean update(T entity, boolean partial, String where, Object[] params) {
        var watch = new StopWatch();
        validator.validate(entity, partial);
        UpdateQuery.Statement query = updateQuery.update(entity, partial, where, params);
        int updatedRows = 0;
        try {
            updatedRows = database.operation.update(query.sql, query.params);
            // refer to https://dev.mysql.com/doc/c-api/8.0/en/mysql-affected-rows.html
            // if all columns updated to its current values, the affectedRows will be 0
            return updatedRows == 1;
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("update, sql={}, params={}, updatedRows={}, elapsed={}", query.sql, new SQLParams(database.operation.enumMapper, query.params), updatedRows, elapsed);
            database.track(elapsed, 0, updatedRows, 1);
        }
    }

    @Override
    public boolean partialUpdate(T entity) {
        return update(entity, true, null, null);
    }

    @Override
    public boolean partialUpdate(T entity, String where, Object... params) {
        return update(entity, true, where, params);
    }

    @Override
    public boolean delete(Object... primaryKeys) {
        var watch = new StopWatch();
        if (primaryKeys.length != selectQuery.primaryKeyColumns) {
            throw new Error(Strings.format("the length of primary keys must match columns, primaryKeys={}, columns={}", primaryKeys.length, selectQuery.primaryKeyColumns));
        }
        int affectedRows = 0;
        try {
            affectedRows = database.operation.update(deleteSQL, primaryKeys);
            if (affectedRows != 1) logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "row is not deleted, result={}", affectedRows);
            return affectedRows == 1;
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("delete, sql={}, params={}, elapsed={}", deleteSQL, new SQLParams(database.operation.enumMapper, primaryKeys), elapsed);
            database.track(elapsed, 0, affectedRows, 1);
        }
    }

    @Override
    public Optional<long[]> batchInsert(List<T> entities) {
        var watch = new StopWatch();
        if (entities.isEmpty()) throw new Error("entities must not be empty");
        String sql = insertQuery.insertSQL;
        List<Object[]> params = new ArrayList<>(entities.size());
        for (T entity : entities) {
            validator.validate(entity, false);
            params.add(insertQuery.params(entity));
        }
        try {
            return database.operation.batchInsert(sql, params, insertQuery.generatedColumn);
        } finally {
            long elapsed = watch.elapsed();
            int size = entities.size();
            logger.debug("batchInsert, sql={}, params={}, size={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), size, elapsed);
            database.track(elapsed, 0, size, size);
        }
    }

    @Override
    public boolean batchInsertIgnore(List<T> entities) {
        var watch = new StopWatch();
        if (entities.isEmpty()) throw new Error("entities must not be empty");
        if (insertQuery.generatedColumn != null) throw new Error("entity must not have auto increment primary key, entityClass=" + entityClass.getCanonicalName());
        String sql = insertQuery.insertIgnoreSQL;
        List<Object[]> params = new ArrayList<>(entities.size());
        for (T entity : entities) {
            validator.validate(entity, false);
            params.add(insertQuery.params(entity));
        }
        boolean inserted = false;   // any row inserted
        try {
            int[] affectedRows = database.operation.batchUpdate(sql, params);
            inserted = batchUpdated(affectedRows);
            return inserted;
        } finally {
            long elapsed = watch.elapsed();
            int size = entities.size();
            logger.debug("batchInsertIgnore, sql={}, params={}, size={}, inserted={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), size, inserted, elapsed);
            database.track(elapsed, 0, inserted ? size : 0, size);
        }
    }

    @Override
    public boolean batchUpsert(List<T> entities) {
        var watch = new StopWatch();
        if (entities.isEmpty()) throw new Error("entities must not be empty");
        if (insertQuery.generatedColumn != null) throw new Error("entity must not have auto increment primary key, entityClass=" + entityClass.getCanonicalName());
        String sql = insertQuery.upsertSQL;
        List<Object[]> params = new ArrayList<>(entities.size());
        for (T entity : entities) {
            validator.validate(entity, false);
            params.add(insertQuery.params(entity));
        }
        boolean updated = false;
        try {
            int[] affectedRows = database.operation.batchUpdate(sql, params);
            updated = batchUpdated(affectedRows);
            return updated;
        } finally {
            long elapsed = watch.elapsed();
            int size = entities.size();
            logger.debug("batchUpsert, sql={}, params={}, size={}, updated={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), size, updated, elapsed);
            database.track(elapsed, 0, updated ? size : 0, size);
        }
    }

    private boolean batchUpdated(int[] affectedRows) {
        // refer to com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchWithMultiValuesClause Line 612
        // only need to check first value
        // HSQL actually returns accurate affected rows for batch, so also check if > 0 to make unit test correct
        for (int affectedRow : affectedRows) {
            if (affectedRow == Statement.SUCCESS_NO_INFO) return true;
            if (affectedRow > 0) return true;
        }
        return false;
    }

    @Override
    public boolean batchDelete(List<?> primaryKeys) {
        var watch = new StopWatch();
        if (primaryKeys.isEmpty()) throw new Error("primaryKeys must not be empty");
        List<Object[]> params = batchDeleteParams(primaryKeys);
        int deletedRows = 0;
        try {
            int[] affectedRows = database.operation.batchUpdate(deleteSQL, params);
            deletedRows = Arrays.stream(affectedRows).sum();
            if (deletedRows != primaryKeys.size()) logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "some rows are not deleted, results={}", Arrays.toString(affectedRows));
            return deletedRows > 0;
        } finally {
            long elapsed = watch.elapsed();
            int size = primaryKeys.size();
            logger.debug("batchDelete, sql={}, params={}, size={}, elapsed={}", deleteSQL, new SQLBatchParams(database.operation.enumMapper, params), size, elapsed);
            database.track(elapsed, 0, deletedRows, size);
        }
    }

    List<Object[]> batchDeleteParams(List<?> primaryKeys) {
        List<Object[]> params = new ArrayList<>(primaryKeys.size());
        for (Object primaryKey : primaryKeys) {
            if (primaryKey instanceof final Object[] keys) {
                if (selectQuery.primaryKeyColumns != keys.length)
                    throw new Error(Strings.format("the length of primary keys must match columns, primaryKeys={}, columns={}", keys.length, selectQuery.primaryKeyColumns));
                params.add(keys);
            } else {
                if (selectQuery.primaryKeyColumns != 1)
                    throw new Error(Strings.format("the length of primary keys must match columns, primaryKeys={}, columns={}", 1, selectQuery.primaryKeyColumns));
                params.add(new Object[]{primaryKey});
            }
        }
        return params;
    }
}
