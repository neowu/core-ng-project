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
        int updatedRows = 0;
        String sql = insertQuery.insertIgnoreSQL();
        Object[] params = insertQuery.params(entity);
        try {
            updatedRows = database.operation.update(sql, params);
            return updatedRows == 1;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, updatedRows);
            logger.debug("insertIgnore, sql={}, params={}, elapsed={}", sql, new SQLParams(database.operation.enumMapper, params), elapsed);
            database.checkOperation(elapsed, operations);
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
                logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "updated rows is not 1, rows={}", updatedRows);
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, updatedRows);
            logger.debug("update, sql={}, params={}, elapsed={}", query.sql, new SQLParams(database.operation.enumMapper, query.params), elapsed);
            database.checkOperation(elapsed, operations);
        }
    }

    @Override
    public void partialUpdate(T entity) {
        update(entity, true);
    }

    @Override
    public void delete(Object... primaryKeys) {
        var watch = new StopWatch();
        if (primaryKeys.length != selectQuery.primaryKeyColumns) {
            throw new Error(Strings.format("the length of primary keys does not match columns, primaryKeys={}, columns={}", selectQuery.primaryKeyColumns, primaryKeys.length));
        }
        int deletedRows = 0;
        try {
            deletedRows = database.operation.update(deleteSQL, primaryKeys);
            if (deletedRows != 1)
                logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows is not 1, rows={}", deletedRows);
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, deletedRows);
            logger.debug("delete, sql={}, params={}, elapsed={}", deleteSQL, new SQLParams(database.operation.enumMapper, primaryKeys), elapsed);
            database.checkOperation(elapsed, operations);
        }
    }

    @Override
    public void batchInsert(List<T> entities) {
        var watch = new StopWatch();
        if (entities.isEmpty()) throw new Error("entities must not be empty");
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
            int operations = ActionLogContext.track("db", elapsed, 0, size);
            logger.debug("batchInsert, sql={}, params={}, size={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), size, elapsed);
            database.checkOperation(elapsed, operations);
        }
    }

    @Override
    public int batchInsertIgnore(List<T> entities) {
        var watch = new StopWatch();
        if (entities.isEmpty()) throw new Error("entities must not be empty");
        String sql = insertQuery.insertIgnoreSQL();
        List<Object[]> params = new ArrayList<>(entities.size());
        for (T entity : entities) {
            validator.validate(entity, false);
            params.add(insertQuery.params(entity));
        }
        int insertedRows = 0;
        try {
            int[] results = database.operation.batchUpdate(sql, params);
            insertedRows = insertedRows(results);
            return insertedRows;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, insertedRows);
            logger.debug("batchInsertIgnore, sql={}, params={}, size={}, elapsed={}", sql, new SQLBatchParams(database.operation.enumMapper, params), entities.size(), elapsed);
            database.checkOperation(elapsed, operations);
        }
    }

    int insertedRows(int[] results) {
        int insertedRows = 0;
        for (int result : results) {
            if (result == Statement.SUCCESS_NO_INFO) insertedRows++;    // with insertIgnore, mysql returns -2 if insert succeeds
            else if (result > 0) insertedRows += result;
        }
        return insertedRows;
    }

    @Override
    public void batchDelete(List<?> primaryKeys) {
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
            deletedRows = Arrays.stream(results).sum();
            if (deletedRows != primaryKeys.size())
                logger.warn(errorCode("UNEXPECTED_UPDATE_RESULT"), "deleted rows does not match size of primary keys, rows={}", Arrays.toString(results));
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, deletedRows);
            logger.debug("batchDelete, sql={}, params={}, size={}, elapsed={}", deleteSQL, new SQLBatchParams(database.operation.enumMapper, params), primaryKeys.size(), elapsed);
            database.checkOperation(elapsed, operations);
        }
    }
}
