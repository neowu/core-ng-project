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

    RepositoryImpl(DatabaseImpl database, Class<T> entityClass) {
        this.database = database;
        validator = Validator.of(entityClass);
        insertQuery = new InsertQueryBuilder<>(entityClass).build();
        selectQuery = new SelectQuery<>(entityClass);
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
        String sql = insertQuery.sql;
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
        String sql = insertQuery.insertIgnoreSQL();
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
        String sql = insertQuery.upsertSQL();
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
        return update(entity, false, null, null);
    }

    private boolean update(T entity, boolean partial, String where, Object[] params) {
        var watch = new StopWatch();
        validator.validate(entity, partial);
        UpdateQuery.Statement query = updateQuery.update(entity, partial, where, params);
        int updatedRows = 0;
        try {
            updatedRows = database.operation.update(query.sql, query.params);
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
            if (affectedRows != 1) logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows is not 1, rows={}", affectedRows);
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
        String sql = insertQuery.sql;
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
    public boolean[] batchInsertIgnore(List<T> entities) {
        var watch = new StopWatch();
        if (entities.isEmpty()) throw new Error("entities must not be empty");
        if (insertQuery.generatedColumn != null) throw new Error("entity must not have auto increment primary key, entityClass=" + entityClass.getCanonicalName());
        String sql = insertQuery.insertIgnoreSQL();
        List<Object[]> params = new ArrayList<>(entities.size());
        for (T entity : entities) {
            validator.validate(entity, false);
            params.add(insertQuery.params(entity));
        }
        int insertedRows = 0;
        try {
            int[] affectedRows = database.operation.batchUpdate(sql, params);
            boolean[] results = new boolean[affectedRows.length];
            insertedRows = batchResults(affectedRows, results);
            return results;
        } finally {
            long elapsed = watch.elapsed();
            int size = entities.size();
            logger.debug("batchInsertIgnore, sql={}, params={}, size={}, insertedRows={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), size, insertedRows, elapsed);
            database.track(elapsed, 0, insertedRows, size);
        }
    }

    @Override
    public boolean[] batchUpsert(List<T> entities) {
        var watch = new StopWatch();
        if (entities.isEmpty()) throw new Error("entities must not be empty");
        if (insertQuery.generatedColumn != null) throw new Error("entity must not have auto increment primary key, entityClass=" + entityClass.getCanonicalName());
        String sql = insertQuery.upsertSQL();
        List<Object[]> params = new ArrayList<>(entities.size());
        for (T entity : entities) {
            validator.validate(entity, false);
            params.add(insertQuery.params(entity));
        }
        int updatedRows = 0;
        try {
            int[] affectedRows = database.operation.batchUpdate(sql, params);
            boolean[] results = new boolean[affectedRows.length];
            updatedRows = batchResults(affectedRows, results);
            return results;
        } finally {
            long elapsed = watch.elapsed();
            int size = entities.size();
            logger.debug("batchUpsert, sql={}, params={}, size={}, updatedRows={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), size, updatedRows, elapsed);
            database.track(elapsed, 0, updatedRows, size);
        }
    }

    @Override
    public boolean[] batchDelete(List<?> primaryKeys) {
        var watch = new StopWatch();
        if (primaryKeys.isEmpty()) throw new Error("primaryKeys must not be empty");
        List<Object[]> params = new ArrayList<>(primaryKeys.size());
        for (Object primaryKey : primaryKeys) {
            if (primaryKey instanceof Object[]) {
                params.add((Object[]) primaryKey);
            } else {
                params.add(new Object[]{primaryKey});
            }
        }
        int deletedRows = 0;
        try {
            int[] affectedRows = database.operation.batchUpdate(deleteSQL, params);
            boolean[] results = new boolean[affectedRows.length];
            deletedRows = batchResults(affectedRows, results);
            if (deletedRows != primaryKeys.size()) logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows do not match size of primary keys, results={}", Arrays.toString(affectedRows));
            return results;
        } finally {
            long elapsed = watch.elapsed();
            int size = primaryKeys.size();
            logger.debug("batchDelete, sql={}, params={}, size={}, elapsed={}", deleteSQL, new SQLBatchParams(database.operation.enumMapper, params), size, elapsed);
            database.track(elapsed, 0, deletedRows, size);
        }
    }

    // use procedurally way to produce results to insertedRows and insertedResults in one method, to balance both performance and ease of unit test
    // not recommended applying in application level code
    int batchResults(int[] affectedRows, boolean[] results) {
        int updatedRows = 0;
        for (int i = 0; i < affectedRows.length; i++) {
            int affectedRow = affectedRows[i];
            // with batchInsert, mysql returns -2 if insert succeeds, for batch only
            // refer to com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchedInserts
            // with upsert, returns 1 if inserted, 2 if updated
            if (affectedRow == Statement.SUCCESS_NO_INFO || affectedRow == 1) {
                results[i] = true;  // return true if row is inserted/deleted/updated
                updatedRows++;
            } else if (affectedRow > 0) {
                updatedRows++;
            }
        }
        return updatedRows;
    }
}
