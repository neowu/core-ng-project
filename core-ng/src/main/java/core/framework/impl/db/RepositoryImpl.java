package core.framework.impl.db;

import core.framework.db.Column;
import core.framework.db.Query;
import core.framework.db.Repository;
import core.framework.impl.validate.Validator;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * @author neo
 */
public final class RepositoryImpl<T> implements Repository<T> {
    private final Logger logger = LoggerFactory.getLogger(RepositoryImpl.class);
    private final DatabaseImpl database;
    private final Validator validator;
    private final SelectQuery<T> selectQuery;
    private final InsertQuery<T> insertQuery;
    private final UpdateQuery<T> updateQuery;
    private final String deleteSQL;
    private final Class<T> entityClass;

    RepositoryImpl(DatabaseImpl database, Class<T> entityClass) {
        this.database = database;
        validator = new Validator(entityClass, field -> field.getDeclaredAnnotation(Column.class).name());
        insertQuery = new InsertQueryBuilder<>(entityClass).build();
        selectQuery = new SelectQuery<>(entityClass, database.vendor);
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
        String sql = selectQuery.getSQL;
        return database.selectOne(sql, entityClass, primaryKeys);
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
            ActionLogContext.track("db", elapsed, 0, 1);
            logger.debug("insert, sql={}, params={}, elapsed={}", sql, new SQLParams(database.operation.enumMapper, params), elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void update(T entity) {
        update(entity, false);
    }

    private void update(T entity, boolean partial) {
        var watch = new StopWatch();
        validator.validate(entity, partial);
        UpdateQuery.Statement query = updateQuery.update(entity, partial);
        int updatedRows = 0;
        try {
            updatedRows = database.operation.update(query.sql, query.params);
            if (updatedRows != 1)
                logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "updated rows is not 1, rows={}", updatedRows);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("db", elapsed, 0, updatedRows);
            logger.debug("update, sql={}, params={}, elapsed={}", query.sql, new SQLParams(database.operation.enumMapper, query.params), elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void partialUpdate(T entity) {
        update(entity, true);
    }

    @Override
    public void delete(Object... primaryKeys) {
        var watch = new StopWatch();
        int deletedRows = 0;
        try {
            deletedRows = database.operation.update(deleteSQL, primaryKeys);
            if (deletedRows != 1)
                logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows is not 1, rows={}", deletedRows);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("db", elapsed, 0, deletedRows);
            logger.debug("delete, sql={}, params={}, elapsed={}", deleteSQL, new SQLParams(database.operation.enumMapper, primaryKeys), elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void batchInsert(List<T> entities) {
        var watch = new StopWatch();
        String sql = insertQuery.sql;
        List<Object[]> params = new ArrayList<>(entities.size());
        for (T entity : entities) {
            validator.validate(entity, false);
            params.add(insertQuery.params(entity));
        }
        try {
            database.operation.batchUpdate(sql, params);
        } finally {
            long elapsed = watch.elapsed();
            int size = entities.size();
            ActionLogContext.track("db", elapsed, 0, size);
            logger.debug("batchInsert, sql={}, params={}, size={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), size, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void batchDelete(List<?> primaryKeys) {
        var watch = new StopWatch();
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
            deletedRows = Arrays.stream(results).sum();
            if (deletedRows != primaryKeys.size())
                logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows does not match size of primary keys, rows={}", Arrays.toString(results));
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("db", elapsed, 0, deletedRows);
            logger.debug("batchDelete, sql={}, params={}, size={}, elapsed={}", deleteSQL, new SQLBatchParams(database.operation.enumMapper, params), primaryKeys.size(), elapsed);
            checkSlowOperation(elapsed);
        }
    }

    private void checkSlowOperation(long elapsed) {
        if (elapsed > database.slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_DB"), "slow db operation, elapsed={}", elapsed);
        }
    }
}
