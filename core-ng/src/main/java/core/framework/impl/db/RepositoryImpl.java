package core.framework.impl.db;

import core.framework.api.db.Query;
import core.framework.api.db.Repository;
import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
import core.framework.api.util.Lists;
import core.framework.api.util.StopWatch;
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
    private final SelectQuery selectQuery;
    private final InsertQuery<T> insertQuery;
    private final UpdateQuery<T> updateQuery;
    private final String deleteSQL;
    private final RowMapper<T> rowMapper;

    RepositoryImpl(DatabaseImpl database, Class<T> entityClass, RowMapper<T> rowMapper) {
        this.database = database;
        validator = new RepositoryEntityValidator<>(entityClass);
        insertQuery = new InsertQuery<>(entityClass);
        selectQuery = new SelectQuery(entityClass);
        updateQuery = new UpdateQuery<>(entityClass);
        deleteSQL = DeleteQueryBuilder.build(entityClass);
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> select(Query query) {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.sql(query.where, query.skip, query.limit);
        Object[] params = selectQuery.params(query);
        List<T> results = null;
        try {
            results = database.operation.select(sql, rowMapper, params);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("select, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            checkSlowOperation(elapsedTime);
            if (results != null) checkTooManyRows(results.size());
        }
    }

    @Override
    public Optional<T> selectOne(String where, Object... params) {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.sql(where, null, null);
        try {
            return database.operation.selectOne(sql, rowMapper, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectOne, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Optional<T> get(Object... primaryKeys) {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.selectByPrimaryKeys;
        try {
            return database.operation.selectOne(sql, rowMapper, primaryKeys);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
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
            return database.operation.insert(sql, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("insert, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void update(T entity) {
        StopWatch watch = new StopWatch();
        validator.partialValidate(entity);
        UpdateQuery.Query query = updateQuery.query(entity);
        try {
            int updatedRows = database.operation.update(query.sql, query.params);
            if (updatedRows != 1)
                logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "updated rows is not 1, rows={}", updatedRows);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("update, sql={}, params={}, elapsedTime={}", query.sql, query.params, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void delete(Object... primaryKeys) {
        StopWatch watch = new StopWatch();
        try {
            int deletedRows = database.operation.update(deleteSQL, primaryKeys);
            if (deletedRows != 1)
                logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows is not 1, rows={}", deletedRows);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("delete, sql={}, params={}, elapsedTime={}", deleteSQL, primaryKeys, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void batchInsert(List<T> entities) {
        StopWatch watch = new StopWatch();
        entities.forEach(validator::validate);
        String sql = insertQuery.sql;
        List<Object[]> params = entities.stream()
            .map(insertQuery::params)
            .collect(Collectors.toList());
        try {
            database.operation.batchUpdate(sql, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
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
        try {
            int[] deletedRows = database.operation.batchUpdate(deleteSQL, params);
            for (int deletedRow : deletedRows) {
                if (deletedRow != 1) {
                    logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows is not 1, rows={}", Arrays.toString(deletedRows));
                    break;
                }
            }
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("delete, sql={}, size={}, elapsedTime={}", deleteSQL, primaryKeys.size(), elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private void checkTooManyRows(int size) {
        if (size > database.tooManyRowsReturnedThreshold) {
            logger.warn(Markers.errorCode("TOO_MANY_ROWS_RETURNED"), "too many rows returned, returnedRows={}", size);
        }
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > database.slowOperationThresholdInMs) {
            logger.warn(Markers.errorCode("SLOW_DB"), "slow db operation, elapsedTime={}", elapsedTime);
        }
    }
}
