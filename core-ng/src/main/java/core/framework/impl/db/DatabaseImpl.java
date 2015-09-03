package core.framework.impl.db;

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
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * @author neo
 */
public final class DatabaseImpl implements Database {
    private final Logger logger = LoggerFactory.getLogger(DatabaseImpl.class);

    public final TransactionManager transactionManager;
    public final Pool<Connection> pool;
    public long slowQueryThresholdInMs = Duration.ofSeconds(5).toMillis();
    public int tooManyRowsReturnedThreshold = 1000;
    private final Map<Class, RowMapper> viewRowMappers = Maps.newHashMap();

    private Duration timeout;
    private Driver driver;
    private String url;
    private final Properties info = new Properties();

    public DatabaseImpl() {
        pool = new Pool<>(this::createConnection, Connection::close);
        pool.name("db");
        pool.size(5, 50);    // default optimization for AWS medium/large instances
        pool.maxIdleTime(Duration.ofHours(2));
        timeout(Duration.ofSeconds(30));
        transactionManager = new TransactionManager(pool);
    }

    private Connection createConnection() {
        if (url == null) throw new Error("url must not be null");
        try {
            return driver.connect(url, info);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public void close() {
        logger.info("close database client, url={}", url);
        pool.close();
    }

    public void user(String user) {
        info.put("user", user);
    }

    public void password(String password) {
        info.put("password", password);
    }

    public void timeout(Duration timeout) {
        this.timeout = timeout;
        pool.checkoutTimeout(timeout);

        if (url != null && url.startsWith("jdbc:mysql:")) {
            info.put("connectTimeout", String.valueOf(timeout.toMillis()));
            info.put("socketTimeout", String.valueOf(timeout.toMillis()));
        }
    }

    public void url(String url) {
        if (!url.startsWith("jdbc:")) throw Exceptions.error("jdbc url must start with \"jdbc:\", url={}", url);

        logger.info("set database connection url, url={}", url);
        this.url = url;
        try {
            if (url.startsWith("jdbc:mysql://")) {
                driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
                timeout(timeout);
            } else if (url.startsWith("jdbc:hsqldb:")) {
                driver = (Driver) Class.forName("org.hsqldb.jdbc.JDBCDriver").newInstance();
            } else {
                throw Exceptions.error("not supported database, please contact arch team, url={}", url);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    public <T> void view(Class<T> viewClass) {
        new DatabaseClassValidator(viewClass).validateViewClass();
        registerViewClass(viewClass);
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        StopWatch watch = new StopWatch();
        try {
            RepositoryEntityValidator<T> validator = new RepositoryEntityValidator<>(entityClass);
            RowMapper<T> mapper = registerViewClass(entityClass);
            return new RepositoryImpl<>(this, validator, entityClass, mapper);
        } finally {
            logger.info("create db repository, entityClass={}, elapsedTime={}", entityClass.getCanonicalName(), watch.elapsedTime());
        }
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
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout((int) timeout.getSeconds());
            PreparedStatements.setParams(statement, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            ConnectionHelper.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    int[] batchUpdate(String sql, List<List<Object>> params) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout((int) timeout.getSeconds());
            for (List<Object> batchParams : params) {
                PreparedStatements.setParams(statement, batchParams);
                statement.addBatch();
            }
            return statement.executeBatch();
        } catch (SQLException e) {
            ConnectionHelper.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    <T> List<T> executeQuery(String sql, List<Object> params, RowMapper<T> mapper) {
        if (sql.contains("*"))
            throw Exceptions.error("select statement should not contain wildcard(*), please only select columns needed, sql={}", sql);
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout((int) timeout.getSeconds());
            PreparedStatements.setParams(statement, params);
            return fetchResultSet(statement, mapper);
        } catch (SQLException e) {
            ConnectionHelper.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    Optional<Long> insert(String sql, List<Object> params) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setQueryTimeout((int) timeout.getSeconds());
            PreparedStatements.setParams(statement, params);
            statement.executeUpdate();
            return fetchGeneratedKey(statement);
        } catch (SQLException e) {
            ConnectionHelper.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    <T> Optional<T> executeSelectOneQuery(String sql, List<Object> params, RowMapper<T> mapper) {
        List<T> results = executeQuery(sql, params, mapper);
        if (results.isEmpty()) return Optional.empty();
        if (results.size() > 1) throw new Error("more than one row returned, size=" + results.size());
        return Optional.ofNullable(results.get(0));
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
