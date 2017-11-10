package core.framework.impl.db;

import core.framework.db.Query;
import core.framework.db.Repository;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.util.Lists;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public final class RepositoryImpl<T> implements Repository<T> {
    private final Logger logger = LoggerFactory.getLogger(RepositoryImpl.class);
    private final DatabaseImpl database;
    private final RepositoryEntityValidator<T> validator;
    private final SelectQuery<T> selectQuery;
    private final InsertQuery<T> insertQuery;
    private final UpdateQuery<T> updateQuery;
    private final String deleteSQL;
    private final RowMapper<T> rowMapper;

    RepositoryImpl(DatabaseImpl database, Class<T> entityClass, RowMapper<T> rowMapper) {
        this.database = database;
        validator = new RepositoryEntityValidator<>(entityClass);
        insertQuery = new InsertQuery<>(entityClass);
        selectQuery = new SelectQuery<>(entityClass, database.vendor);
        updateQuery = new UpdateQueryBuilder<>(entityClass).build();
        deleteSQL = DeleteQueryBuilder.build(entityClass);
        this.rowMapper = rowMapper;
    }

    @Override
    public Query<T> select() {
        return new QueryImpl<>(this, selectQuery.dialect);
    }

    List<T> fetch(String sql, Object... params) {
        StopWatch watch = new StopWatch();
        int returnedRows = 0;
        try {
            List<T> results = database.operation.select(sql, rowMapper, params);
            returnedRows = results.size();
            checkTooManyRowsReturned(returnedRows);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, returnedRows, 0);
            logger.debug("fetch, sql={}, params={}, returnedRows={}, elapsedTime={}", sql, params, returnedRows, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public int count(String where, Object... params) {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.countSQL(where);
        try {
            return database.operation.selectOne(sql, DatabaseImpl.ROW_MAPPER_INTEGER, params).orElseThrow(() -> new Error("unexpected result"));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, 1, 0);
            logger.debug("count, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Optional<T> selectOne(String where, Object... params) {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.selectSQL(where);
        int returnedRows = 0;
        try {
            Optional<T> result = database.operation.selectOne(sql, rowMapper, params);
            if (result.isPresent()) returnedRows = 1;
            return result;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, returnedRows, 0);
            logger.debug("selectOne, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Optional<T> get(Object... primaryKeys) {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.getSQL;
        int returnedRows = 0;
        try {
            Optional<T> result = database.operation.selectOne(sql, rowMapper, primaryKeys);
            if (result.isPresent()) returnedRows = 1;
            return result;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, returnedRows, 0);
            logger.debug("get, sql={}, params={}, elapsedTime={}", sql, primaryKeys, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Optional<Long> insert(T entity) {
        StopWatch watch = new StopWatch();
        validator.validate(entity);
        String sql = insertQuery.sql;
        Object[] params = insertQuery.params(entity);
        try {
            return database.operation.insert(sql, params, insertQuery.generatedColumn);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, 0, 1);
            logger.debug("insert, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void update(T entity) {
        StopWatch watch = new StopWatch();
        validator.partialValidate(entity);
        UpdateQuery.Statement query = updateQuery.update(entity);
        int updatedRows = 0;
        try {
            updatedRows = database.operation.update(query.sql, query.params);
            if (updatedRows != 1)
                logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "updated rows is not 1, rows={}", updatedRows);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, 0, updatedRows);
            logger.debug("update, sql={}, params={}, elapsedTime={}", query.sql, query.params, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void delete(Object... primaryKeys) {
        StopWatch watch = new StopWatch();
        int deletedRows = 0;
        try {
            deletedRows = database.operation.update(deleteSQL, primaryKeys);
            if (deletedRows != 1)
                logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows is not 1, rows={}", deletedRows);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, 0, deletedRows);
            logger.debug("delete, sql={}, params={}, elapsedTime={}", deleteSQL, primaryKeys, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void batchInsert(List<T> entities) {
        StopWatch watch = new StopWatch();
        entities.forEach(validator::validate);
        String sql = insertQuery.sql;
        List<Object[]> params = entities.stream().map(insertQuery::params).collect(Collectors.toList());
        try {
            database.operation.batchUpdate(sql, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, 0, entities.size());
            logger.debug("batch insert, sql={}, size={}, elapsedTime={}", sql, entities.size(), elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void batchDelete(List<?> primaryKeys) {
        StopWatch watch = new StopWatch();
        List<Object[]> params = Lists.newArrayList();
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
            for (int result : results) {
                if (result != 1) {
                    logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows is not 1, rows={}", Arrays.toString(results));
                    break;
                }
            }
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, 0, deletedRows);
            logger.debug("delete, sql={}, size={}, elapsedTime={}", deleteSQL, primaryKeys.size(), elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private void checkTooManyRowsReturned(int size) {
        if (size > database.tooManyRowsReturnedThreshold) {
            logger.warn(Markers.errorCode("TOO_MANY_ROWS_RETURNED"), "too many rows returned, returnedRows={}", size);
        }
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > database.slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_DB"), "slow db operation, elapsedTime={}", elapsedTime);
        }
    }
}
