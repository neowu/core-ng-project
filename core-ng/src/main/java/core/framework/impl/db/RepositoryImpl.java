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
    private final Class<T> entityClass;

    RepositoryImpl(DatabaseImpl database, Class<T> entityClass) {
        this.database = database;
        validator = new RepositoryEntityValidator<>(entityClass);
        insertQuery = new InsertQuery<>(entityClass);
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
            logger.debug("insert, sql={}, params={}, elapsedTime={}", sql, new SQLParams(database.operation.enumMapper, params), elapsedTime);
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
            logger.debug("update, sql={}, params={}, elapsedTime={}", query.sql, new SQLParams(database.operation.enumMapper, query.params), elapsedTime);
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
            logger.debug("delete, sql={}, params={}, elapsedTime={}", deleteSQL, new SQLParams(database.operation.enumMapper, primaryKeys), elapsedTime);
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
            int size = entities.size();
            ActionLogContext.track("db", elapsedTime, 0, size);
            logger.debug("batchInsert, sql={}, size={}, elapsedTime={}", sql, size, elapsedTime);
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
            if (deletedRows != primaryKeys.size())
                logger.warn(Markers.errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows is not 1, rows={}", Arrays.toString(results));
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, 0, deletedRows);
            logger.debug("batchDelete, sql={}, size={}, elapsedTime={}", deleteSQL, primaryKeys.size(), elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > database.slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_DB"), "slow db operation, elapsedTime={}", elapsedTime);
        }
    }
}
