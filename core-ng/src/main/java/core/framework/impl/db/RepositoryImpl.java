package core.framework.impl.db;

import core.framework.api.db.Query;
import core.framework.api.db.Repository;
import core.framework.api.db.RowMapper;
import core.framework.api.log.ActionLogContext;
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
    private final SelectQueryBuilder selectQuery;
    private final InsertQueryBuilder insertQuery;
    private final UpdateQueryBuilder updateQuery;
    private final DeleteQueryBuilder deleteQuery;
    private final RowMapper<T> rowMapper;

    RepositoryImpl(DatabaseImpl database, RepositoryEntityValidator<T> validator, Class<T> entityClass, RowMapper<T> rowMapper) {
        this.database = database;
        this.validator = validator;
        insertQuery = new InsertQueryBuilder(entityClass);   //TODO: use javaassit to build code for all query builder
        selectQuery = new SelectQueryBuilder(entityClass);
        updateQuery = new UpdateQueryBuilder(entityClass);
        deleteQuery = new DeleteQueryBuilder(entityClass);
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> selectAll() {
        StopWatch watch = new StopWatch();
        Query query = selectQuery.all();
        String sql = query.statement();
        List<T> results = null;
        try {
            results = database.executeQuery(sql, query.params, rowMapper);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectAll, sql={}, params={}, elapsedTime={}", sql, query.params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
            if (results != null && results.size() > database.tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

    @Override
    public List<T> select(String whereClause, Object... params) {
        StopWatch watch = new StopWatch();
        Query query = selectQuery.where(whereClause, params);
        String sql = query.statement();
        List<T> results = null;
        try {
            results = database.executeQuery(sql, query.params, rowMapper);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("select, sql={}, params={}, elapsedTime={}", sql, query.params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
            if (results != null && results.size() > database.tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

    @Override
    public Optional<T> selectOne(String whereClause, Object... params) {
        StopWatch watch = new StopWatch();
        Query query = selectQuery.where(whereClause, params);
        String sql = query.statement();
        try {
            return database.executeSelectOneQuery(sql, query.params, rowMapper);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectOne, sql={}, params={}, elapsedTime={}", sql, query.params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public Optional<T> get(Object... primaryKeys) {
        StopWatch watch = new StopWatch();
        Query query = selectQuery.byPK(primaryKeys);
        String sql = query.statement();
        try {
            return database.executeSelectOneQuery(sql, query.params, rowMapper);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("get, sql={}, params={}, elapsedTime={}", sql, query.params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public Optional<Long> insert(T entity) {
        StopWatch watch = new StopWatch();
        validator.validate(entity);
        String sql = insertQuery.sql;
        List<Object> params = insertQuery.params(entity);
        try {
            return database.insert(sql, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("insert, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public void update(T entity) {
        StopWatch watch = new StopWatch();
        validator.partialValidate(entity);
        Query query = updateQuery.query(entity);
        String sql = query.statement();
        try {
            int updatedRows = database.update(sql, query.params);
            if (updatedRows != 1)
                logger.warn("updated rows is not 1, rows={}", updatedRows);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("update, sql={}, params={}, elapsedTime={}", sql, query.params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public void delete(Object... primaryKeys) {
        StopWatch watch = new StopWatch();
        List<Object> params = Lists.newArrayList(primaryKeys);
        String sql = deleteQuery.sql;
        try {
            int deletedRows = database.update(sql, params);
            if (deletedRows != 1)
                logger.warn("deleted rows is not 1, rows={}", deletedRows);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("delete, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public void batchInsert(List<T> entities) {
        StopWatch watch = new StopWatch();
        entities.forEach(validator::validate);
        String sql = insertQuery.sql;
        List<List<Object>> params = entities.stream()
            .map(insertQuery::params)
            .collect(Collectors.toList());
        try {
            database.batchUpdate(sql, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("batch insert, sql={}, size={}, elapsedTime={}", sql, entities.size(), elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public void batchDelete(List<?> primaryKeys) {
        StopWatch watch = new StopWatch();
        String sql = deleteQuery.sql;
        List<List<Object>> params = primaryKeys.stream()
            .map(Lists::newArrayList)
            .collect(Collectors.toList());
        try {
            int[] deletedRows = database.batchUpdate(sql, params);
            for (int deletedRow : deletedRows) {
                if (deletedRow != 1)
                    logger.warn("deleted rows is not 1, rows={}", Arrays.toString(deletedRows));
            }
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("delete, sql={}, size={}, elapsedTime={}", sql, primaryKeys.size(), elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }
}
