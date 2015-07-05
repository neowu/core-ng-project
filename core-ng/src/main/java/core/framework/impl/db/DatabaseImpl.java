package core.framework.impl.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.management.NullManagementCoordinator;
import core.framework.api.db.Database;
import core.framework.api.db.Query;
import core.framework.api.db.Repository;
import core.framework.api.db.RowMapper;
import core.framework.api.db.Transaction;
import core.framework.api.db.UncheckedSQLException;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class DatabaseImpl implements Database {
    static {
        // disable c3p0 jmx bean
        System.setProperty("com.mchange.v2.c3p0.management.ManagementCoordinator", NullManagementCoordinator.class.getName());
    }

    private final Logger logger = LoggerFactory.getLogger(DatabaseImpl.class);

    public final TransactionManager transactionManager;
    public final ComboPooledDataSource dataSource;
    public long slowQueryThresholdInMs = Duration.ofSeconds(7).toMillis();
    public int tooManyRowsReturnedThreshold = 2000;
    private int timeoutInSeconds = 60;
    private final Map<Class, RowMapper> viewRowMappers = Maps.newHashMap();

    public DatabaseImpl() {
        dataSource = new ComboPooledDataSource();
        dataSource.setTestConnectionOnCheckout(true);
        dataSource.setCheckoutTimeout(timeoutInSeconds * 1000);
        transactionManager = new TransactionManager(dataSource);
    }

    public void shutdown() {
        logger.info("shutdown database connection pool, url={}", dataSource.getJdbcUrl());
        dataSource.close();
    }

    public DatabaseImpl url(String url) {
        if (!url.startsWith("jdbc:")) throw Exceptions.error("jdbc url must start with \"jdbc:\", url={}", url);

        logger.info("set database connection pool url, url={}", url);
        dataSource.setJdbcUrl(url);
        try {
            if (url.startsWith("jdbc:mysql:")) {
                dataSource.setDriverClass("com.mysql.jdbc.Driver");
                dataSource.setPreferredTestQuery("select 1");
            } else if (url.startsWith("jdbc:hsqldb:")) {
                dataSource.setDriverClass("org.hsqldb.jdbc.JDBCDriver");
            } else if (url.startsWith("jdbc:sqlserver")) {
                dataSource.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                dataSource.setPreferredTestQuery("select 1");
            } else {
                throw Exceptions.error("not supported database, please contact arch team, url={}", url);
            }
        } catch (PropertyVetoException e) {
            throw new Error(e);
        }
        return this;
    }

    public <T> DatabaseImpl view(Class<T> viewClass) {
        new DatabaseClassValidator(viewClass).validateViewClass();
        registerViewClass(viewClass);
        return this;
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        logger.info("create db repository, entityClass={}", entityClass.getCanonicalName());
        RepositoryEntityValidator<T> validator = new RepositoryEntityValidator<>(entityClass);
        RowMapper<T> mapper = registerViewClass(entityClass);
        return new RepositoryImpl<>(this, validator, entityClass, mapper);
    }

    public void timeout(Duration timeout) {
        this.timeoutInSeconds = (int) timeout.getSeconds();
        dataSource.setCheckoutTimeout((int) timeout.toMillis());
    }

    @Override
    public Transaction beginTransaction() {
        return transactionManager.beginTransaction();
    }

    @Override
    public <T> List<T> select(Query query, RowMapper<T> mapper) {
        StopWatch watch = new StopWatch();
        String sql = query.statement();
        List<T> results = null;
        try {
            results = executeQuery(sql, query.params, mapper);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("select, sql={}, params={}, elapsedTime={}", sql, query.params, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
            if (results != null && results.size() > tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

    @Override
    public <T> List<T> select(Query query, Class<T> viewClass) {
        return select(query, viewRowMapper(viewClass));
    }

    @Override
    public <T> Optional<T> selectOne(Query query, RowMapper<T> mapper) {
        StopWatch watch = new StopWatch();
        String sql = query.statement();
        try {
            return executeSelectOneQuery(sql, query.params, mapper);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectOne, sql={}, params={}, elapsedTime={}", sql, query.params, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public Optional<String> selectString(String sql, Object... params) {
        return selectOne(Query.query(sql, params), row -> row.getString(1));
    }

    @Override
    public Optional<Integer> selectInt(String sql, Object... params) {
        return selectOne(Query.query(sql, params), row -> row.getInt(1));
    }

    @Override
    public Optional<Long> selectLong(String sql, Object... params) {
        return selectOne(Query.query(sql, params), row -> row.getLong(1));
    }

    @Override
    public <T> Optional<T> selectOne(Query query, Class<T> viewClass) {
        return selectOne(query, viewRowMapper(viewClass));
    }

    @Override
    public int execute(Query query) {
        StopWatch watch = new StopWatch();
        String sql = query.statement();
        try {
            return update(sql, query.params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("execute, sql={}, params={}, elapsedTime={}", sql, query.params, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public int execute(String sql, Object... params) {
        return execute(Query.query(sql, params));
    }

    int update(String sql, List<Object> params) {
        Connection connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(timeoutInSeconds);
            PreparedStatements.setParams(statement, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    int[] batchUpdate(String sql, List<List<Object>> params) {
        Connection connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(timeoutInSeconds);
            for (List<Object> batchParams : params) {
                PreparedStatements.setParams(statement, batchParams);
                statement.addBatch();
            }
            return statement.executeBatch();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    <T> Optional<T> executeSelectOneQuery(String sql, List<Object> params, RowMapper<T> mapper) {
        List<T> results = executeQuery(sql, params, mapper);
        if (results.isEmpty()) return Optional.empty();
        if (results.size() > 1) throw new Error("more than one row returned, size=" + results.size());
        return Optional.of(results.get(0));
    }

    <T> List<T> executeQuery(String sql, List<Object> params, RowMapper<T> mapper) {
        if (sql.contains("*"))
            throw Exceptions.error("select statement should not contain wildcard(*), please only select columns needed, sql={}", sql);
        Connection connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(timeoutInSeconds);
            PreparedStatements.setParams(statement, params);
            return fetchResultSet(statement, mapper);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    Optional<Long> insert(String sql, List<Object> params) {
        Connection connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setQueryTimeout(timeoutInSeconds);
            PreparedStatements.setParams(statement, params);
            statement.executeUpdate();
            return fetchGeneratedKey(statement);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    private <T> List<T> fetchResultSet(PreparedStatement statement, RowMapper<T> mapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return new ResultSetMapper(resultSet).map(mapper);
        }
    }

    private Optional<Long> fetchGeneratedKey(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return Optional.of(keys.getLong(1));
            }
        }
        return Optional.empty();
    }

    private <T> RowMapper<T> viewRowMapper(Class<T> viewClass) {
        @SuppressWarnings("unchecked")
        RowMapper<T> mapper = viewRowMappers.get(viewClass);
        if (mapper == null)
            throw Exceptions.error("view class is not registered, please register in module by db().view(), viewClass={}", viewClass.getCanonicalName());
        return mapper;
    }


    private <T> RowMapper<T> registerViewClass(Class<T> viewClass) {
        if (viewRowMappers.containsKey(viewClass)) {
            throw Exceptions.error("duplicated view class found, viewClass={}", viewClass.getCanonicalName());
        }
        RowMapper<T> mapper = new ViewRowMapperBuilder<>(viewClass).build();
        viewRowMappers.put(viewClass, mapper);
        return mapper;
    }
}
