package core.framework.internal.db;

import core.framework.db.Query;
import core.framework.db.Repository;
import core.framework.internal.validate.Validator;
import core.framework.log.ActionLogContext;
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
            int operations = ActionLogContext.track("db", elapsed, 0, 1);
            logger.debug("insert, sql={}, params={}, elapsed={}", sql, new SQLParams(database.operation.enumMapper, params), elapsed);
            database.checkOperation(elapsed, operations);
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
            int operations = ActionLogContext.track("db", elapsed, 0, insertedRows);
            logger.debug("insertIgnore, sql={}, params={}, insertedRows={}, elapsed={}", sql, new SQLParams(database.operation.enumMapper, params), insertedRows, elapsed);
            database.checkOperation(elapsed, operations);
        }
    }

    @Override
    public boolean update(T entity) {
        int updatedRows = update(entity, false, null, null);
        if (updatedRows != 1) logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "updated rows is not 1, rows={}", updatedRows);
        return updatedRows == 1;
    }

    private int update(T entity, boolean partial, String where, Object[] params) {
        var watch = new StopWatch();
        validator.validate(entity, partial);
        UpdateQuery.Statement query = updateQuery.update(entity, partial, where, params);
        int updatedRows = 0;
        try {
            updatedRows = database.operation.update(query.sql, query.params);
            return updatedRows;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, updatedRows);
            logger.debug("update, sql={}, params={}, updatedRows={}, elapsed={}", query.sql, new SQLParams(database.operation.enumMapper, query.params), updatedRows, elapsed);
            database.checkOperation(elapsed, operations);
        }
    }

    @Override
    public boolean partialUpdate(T entity) {
        int updatedRows = update(entity, true, null, null);
        if (updatedRows != 1) logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "updated rows is not 1, rows={}", updatedRows);
        return updatedRows == 1;
    }

    @Override
    public boolean partialUpdate(T entity, String where, Object... params) {
        int updatedRows = update(entity, true, where, params);
        return updatedRows == 1;
    }

    @Override
    public boolean delete(Object... primaryKeys) {
        var watch = new StopWatch();
        if (primaryKeys.length != selectQuery.primaryKeyColumns) {
            throw new Error(Strings.format("the length of primary keys must match columns, primaryKeys={}, columns={}", primaryKeys.length, selectQuery.primaryKeyColumns));
        }
        int deletedRows = 0;
        try {
            deletedRows = database.operation.update(deleteSQL, primaryKeys);
            if (deletedRows != 1) logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows is not 1, rows={}", deletedRows);
            return deletedRows == 1;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, deletedRows);
            logger.debug("delete, sql={}, params={}, elapsed={}", deleteSQL, new SQLParams(database.operation.enumMapper, primaryKeys), elapsed);
            database.checkOperation(elapsed, operations);
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
            int operations = ActionLogContext.track("db", elapsed, 0, size);
            logger.debug("batchInsert, sql={}, params={}, size={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), size, elapsed);
            database.checkOperation(elapsed, operations);
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
            int[] results = database.operation.batchUpdate(sql, params);
            boolean[] insertedResults = new boolean[results.length];
            insertedRows = updatedResults(results, insertedResults);
            return insertedResults;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, insertedRows);
            logger.debug("batchInsertIgnore, sql={}, params={}, size={}, insertedRows={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), entities.size(), insertedRows, elapsed);
            database.checkOperation(elapsed, operations);
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
            int[] results = database.operation.batchUpdate(deleteSQL, params);
            boolean[] deletedResults = new boolean[results.length];
            deletedRows = updatedResults(results, deletedResults);
            if (deletedRows != primaryKeys.size()) logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows do not match size of primary keys, rows={}", Arrays.toString(results));
            return deletedResults;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, deletedRows);
            logger.debug("batchDelete, sql={}, params={}, size={}, elapsed={}", deleteSQL, new SQLBatchParams(database.operation.enumMapper, params), primaryKeys.size(), elapsed);
            database.checkOperation(elapsed, operations);
        }
    }

    // use procedurally way to produce results to insertedRows and insertedResults in one method, to balance both performance and ease of unit test
    // not recommended applying in application level code
    int updatedResults(int[] results, boolean[] updatedResults) {
        int updatedRows = 0;
        for (int i = 0; i < results.length; i++) {
            int result = results[i];
            // with insertIgnore, mysql returns -2 if insert succeeds, for batch only
            // refer to com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchedInserts
            if (result == Statement.SUCCESS_NO_INFO || result > 0) {
                updatedResults[i] = true;
                updatedRows++;
            }
        }
        return updatedRows;
    }
}
