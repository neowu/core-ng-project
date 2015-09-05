package core.framework.impl.db;

import core.framework.api.db.Repository;
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
    private final SelectQuery selectQuery;
    private final InsertQuery<T> insertQuery;
    private final UpdateQuery<T> updateQuery;
    private final String deleteSQL;
    private final RowMapper<T> rowMapper;

    RepositoryImpl(DatabaseImpl database, RepositoryEntityValidator<T> validator, Class<T> entityClass, RowMapper<T> rowMapper) {
        this.database = database;
        this.validator = validator;
        insertQuery = new InsertQuery<>(entityClass);
        selectQuery = new SelectQuery(entityClass);
        updateQuery = new UpdateQuery<>(entityClass);
        deleteSQL = DeleteQueryBuilder.build(entityClass);
        this.rowMapper = rowMapper;
    }

    @Override
    public List<T> selectAll() {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.selectAll;
        List<T> results = null;
        try {
            results = database.executeSelect(sql, rowMapper, null);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectAll, sql={}, elapsedTime={}", sql, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
            if (results != null && results.size() > database.tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

    @Override
    public List<T> select(String whereClause, Object... params) {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.where(whereClause);
        List<T> results = null;
        try {
            results = database.executeSelect(sql, rowMapper, params);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("select, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
            if (results != null && results.size() > database.tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

    @Override
    public Optional<T> selectOne(String whereClause, Object... params) {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.where(whereClause);
        try {
            return database.executeSelectOne(sql, rowMapper, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectOne, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public Optional<T> get(Object... primaryKeys) {
        StopWatch watch = new StopWatch();
        String sql = selectQuery.selectByPrimaryKeys;
        try {
            return database.executeSelectOne(sql, rowMapper, primaryKeys);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("get, sql={}, params={}, elapsedTime={}", sql, primaryKeys, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public Optional<Long> insert(T entity) {
        StopWatch watch = new StopWatch();
        validator.validate(entity);
        String sql = insertQuery.sql;
        Object[] params = insertQuery.params(entity);
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
        UpdateQuery.Query query = updateQuery.query(entity);
        try {
            int updatedRows = database.update(query.sql, query.params);
            if (updatedRows != 1)
                logger.warn("updated rows is not 1, rows={}", updatedRows);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("update, sql={}, params={}, elapsedTime={}", query.sql, query.params, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public void delete(Object... primaryKeys) {
        StopWatch watch = new StopWatch();
        try {
            int deletedRows = database.update(deleteSQL, primaryKeys);
            if (deletedRows != 1)
                logger.warn("deleted rows is not 1, rows={}", deletedRows);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("delete, sql={}, params={}, elapsedTime={}", deleteSQL, primaryKeys, elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
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
        List<Object[]> params = Lists.newArrayList();
        for (Object primaryKey : primaryKeys) {
            if (primaryKey instanceof Object[]) {
                params.add((Object[]) primaryKey);
            } else {
                params.add(new Object[]{primaryKey});
            }
        }
        try {
            int[] deletedRows = database.batchUpdate(deleteSQL, params);
            for (int deletedRow : deletedRows) {
                if (deletedRow != 1)
                    logger.warn("deleted rows is not 1, rows={}", Arrays.toString(deletedRows));
            }
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("delete, sql={}, size={}, elapsedTime={}", deleteSQL, primaryKeys.size(), elapsedTime);
            if (elapsedTime > database.slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }
}
