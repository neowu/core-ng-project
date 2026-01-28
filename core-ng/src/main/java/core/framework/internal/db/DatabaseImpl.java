package core.framework.internal.db;

import core.framework.db.CloudAuthProvider;
import core.framework.db.Database;
import core.framework.db.Dialect;
import core.framework.db.IsolationLevel;
import core.framework.db.Repository;
import core.framework.db.Transaction;
import core.framework.db.UncheckedSQLException;
import core.framework.internal.db.inspector.MySQLQueryAnalyzer;
import core.framework.internal.db.inspector.PostgreSQLQueryAnalyzer;
import core.framework.internal.db.inspector.QueryInspector;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.resource.Pool;
import core.framework.util.StopWatch;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class DatabaseImpl implements Database {
    static {
        // disable unnecessary mysql connection cleanup thread to reduce overhead, refer to PropertyDefinitions.SYSP_disableAbandonedConnectionCleanup
        System.setProperty("com.mysql.cj.disableAbandonedConnectionCleanup", "true");
    }

    public final Pool<Connection> pool;
    public final TransactionManager transactionManager;

    final DatabaseOperation operation;
    private final Logger logger = LoggerFactory.getLogger(DatabaseImpl.class);
    private final Map<Class<?>, RowMapper<?>> rowMappers = new HashMap<>(32);

    public @Nullable String user;
    public @Nullable String password;
    public @Nullable CloudAuthProvider authProvider;
    public @Nullable IsolationLevel isolationLevel;

    private @Nullable String url;
    private @Nullable Properties driverProperties;
    private Duration timeout;
    private Driver driver;
    private QueryInspector inspector;

    public DatabaseImpl(String name) {
        initializeRowMappers();

        pool = new Pool<>(this::createConnection, name);
        pool.size(5, 50);    // default optimization for AWS medium/large instances
        pool.maxIdleTime = Duration.ofHours(2);  // make sure db server does not kill connection shorter than this, e.g. MySQL default wait_timeout is 8 hours
        pool.validator(connection -> connection.isValid(1), Duration.ofSeconds(30));

        transactionManager = new TransactionManager(pool);
        operation = new DatabaseOperation(transactionManager);
        timeout(Duration.ofSeconds(15));
    }

    private void initializeRowMappers() {
        rowMappers.put(String.class, new RowMapper.StringRowMapper());
        rowMappers.put(Integer.class, new RowMapper.IntegerRowMapper());
        rowMappers.put(Long.class, new RowMapper.LongRowMapper());
        rowMappers.put(Double.class, new RowMapper.DoubleRowMapper());
        rowMappers.put(BigDecimal.class, new RowMapper.BigDecimalRowMapper());
        rowMappers.put(Boolean.class, new RowMapper.BooleanRowMapper());
        rowMappers.put(LocalDateTime.class, new RowMapper.LocalDateTimeRowMapper());
        rowMappers.put(LocalDate.class, new RowMapper.LocalDateRowMapper());
        rowMappers.put(ZonedDateTime.class, new RowMapper.ZonedDateTimeRowMapper());
        rowMappers.put(UUID.class, new RowMapper.UUIDRowMapper());
    }

    private Connection createConnection() {
        if (url == null) throw new Error("url must not be null");
        Properties driverProperties = this.driverProperties;
        if (driverProperties == null) {
            driverProperties = driverProperties(url);
            this.driverProperties = driverProperties;
        }
        if (authProvider != null) {
            // properties are thread safe, it's ok to set user/password with multiple threads
            driverProperties.setProperty("user", authProvider.user(operation.dialect));
            driverProperties.setProperty("password", authProvider.accessToken());
        }
        Connection connection = null;
        try {
            connection = driver.connect(url, driverProperties);
            if (isolationLevel != null) connection.setTransactionIsolation(isolationLevel.level);
            return connection;
        } catch (SQLException e) {
            Pool.closeQuietly(connection);
            throw new UncheckedSQLException(e);
        }
    }

    Properties driverProperties(String url) {
        var properties = new Properties();
        if (authProvider == null && user != null) properties.setProperty("user", user);
        if (authProvider == null && password != null) properties.setProperty("password", password);
        if (url.startsWith("jdbc:mysql:")) {
            // refer to https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
            properties.setProperty("connectTimeout", String.valueOf(timeout.toMillis()));
            // add 10s for socket timeout which is read timeout, as all queries have queryTimeout, MySQL will send "KILL QUERY" command to MySQL server
            // otherwise we will see socket timeout exception (com.mysql.cj.exceptions.StatementIsClosedException: No operations allowed after statement closed)
            // refer to com.mysql.cj.CancelQueryTaskImpl
            properties.setProperty("socketTimeout", String.valueOf(timeout.toMillis() + 10_000));
            // refer to https://dev.mysql.com/doc/c-api/8.0/en/mysql-affected-rows.html
            // refer to https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-connp-props-connection.html#cj-conn-prop_useAffectedRows
            // Don't set the CLIENT_FOUND_ROWS flag when connecting to the server (not JDBC-compliant, will break most applications that rely on "found" rows vs. "affected rows" for DML statements),
            // but does cause "correct" update counts from "INSERT ... ON DUPLICATE KEY UPDATE" statements to be returned by the server.
            properties.setProperty("useAffectedRows", "true");
            // refer to com.mysql.cj.protocol.a.NativeProtocol.configureTimeZone,
            // force to UTC, generally on cloud it defaults to UTC, this setting is to make local match cloud
            properties.setProperty("connectionTimeZone", "UTC");
            properties.setProperty("rewriteBatchedStatements", "true");
            properties.setProperty("logger", "Slf4JLogger");
            properties.setProperty("cachePrepStmts", "true");

            // with gcloud/azure iam / clear_text_password plugin, ssl is required, refer to https://cloud.google.com/sql/docs/mysql/authentication
            if (authProvider != null) {
                properties.setProperty(CloudAuthProvider.Provider.CLOUD_AUTH, "true");
            }
            // refer to https://dev.mysql.com/doc/connector-j/en/connector-j-reference-charsets.html
            int index = url.indexOf('?');
            if (index == -1 || url.indexOf("characterEncoding=", index + 1) == -1)
                properties.setProperty("characterEncoding", "utf-8");
        } else if (url.startsWith("jdbc:postgresql:")) {
            // refer to org.postgresql.PGProperty
            properties.setProperty("connectTimeout", String.valueOf(timeout.toSeconds()));
            properties.setProperty("socketTimeout", String.valueOf(timeout.toSeconds()));
            properties.setProperty("reWriteBatchedInserts", "true");
            properties.setProperty("prepareThreshold", "1");
            properties.setProperty("tcpKeepAlive", "true");
            properties.setProperty("ApplicationName", LogManager.APP_NAME);
        }
        return properties;
    }

    public void close() {
        logger.info("close database client, url={}", url);
        pool.close();
    }

    public void timeout(Duration timeout) {
        this.timeout = timeout;
        operation.queryTimeoutInSeconds = (int) timeout.getSeconds();
        pool.checkoutTimeout(timeout);
    }

    public void url(String url) {
        this.url = url;
        driver = driver(url);
        logger.info("set database connection url, url={}, driver={}.{}", url, driver.getMajorVersion(), driver.getMinorVersion());
    }

    private Driver driver(String url) {
        if (url.startsWith("jdbc:mysql:")) {
            operation.dialect = Dialect.MYSQL;
            inspector = new QueryInspector(new MySQLQueryAnalyzer(operation));
            return createDriver("com.mysql.cj.jdbc.Driver");
        } else if (url.startsWith("jdbc:postgresql:")) {
            operation.dialect = Dialect.POSTGRESQL;
            inspector = new QueryInspector(new PostgreSQLQueryAnalyzer(operation));
            return createDriver("org.postgresql.Driver");
        } else if (url.startsWith("jdbc:hsqldb:")) {
            operation.dialect = Dialect.MYSQL;    // unit test use mysql dialect
            inspector = new QueryInspector(null);
            return createDriver("org.hsqldb.jdbc.JDBCDriver");
        } else {
            throw new Error("not supported database, url=" + url);
        }
    }

    private Driver createDriver(String driverClass) {
        try {
            return Class.forName(driverClass).asSubclass(Driver.class).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public <T> void view(Class<T> viewClass) {
        var watch = new StopWatch();
        try {
            new DatabaseClassValidator(viewClass, true).validate();
            registerViewClass(viewClass);
        } finally {
            logger.info("register db view, viewClass={}, elapsed={}", viewClass.getCanonicalName(), watch.elapsed());
        }
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        var watch = new StopWatch();
        try {
            new DatabaseClassValidator(entityClass, false).validate();
            registerViewClass(entityClass);
            return new RepositoryImpl<>(this, entityClass);
        } finally {
            logger.info("register db entity, entityClass={}, elapsed={}", entityClass.getCanonicalName(), watch.elapsed());
        }
    }

    @Override
    public Transaction beginTransaction() {
        return transactionManager.beginTransaction();
    }

    @Override
    public <T> List<T> select(String sql, Class<T> viewClass, Object... params) {
        String inefficientPlan = inspector.inspect(sql, params);

        var watch = new StopWatch();
        int returnedRows = 0;
        try {
            List<T> results = operation.select(sql, rowMapper(viewClass), params);
            returnedRows = results.size();
            return results;
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("select, sql={}, params={}, returnedRows={}, elapsed={}", sql, new SQLParams(operation.enumMapper, params), returnedRows, elapsed);
            boolean slow = track(elapsed, returnedRows, 0, 1);   // check after sql debug log, to make log easier to read
            logQueryPlan(inefficientPlan, slow, sql, params);
        }
    }

    @Override
    public <T> Optional<T> selectOne(String sql, Class<T> viewClass, Object... params) {
        String inefficientPlan = inspector.inspect(sql, params);

        var watch = new StopWatch();
        int returnedRows = 0;
        try {
            Optional<T> result = operation.selectOne(sql, rowMapper(viewClass), params);
            if (result.isPresent()) returnedRows = 1;
            return result;
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("selectOne, sql={}, params={}, returnedRows={}, elapsed={}", sql, new SQLParams(operation.enumMapper, params), returnedRows, elapsed);
            boolean slow = track(elapsed, returnedRows, 0, 1);
            logQueryPlan(inefficientPlan, slow, sql, params);
        }
    }

    @Override
    public int execute(String sql, Object... params) {
        String inefficientPlan = inspector.inspect(sql, params);

        var watch = new StopWatch();
        int affectedRows = 0;
        try {
            affectedRows = operation.update(sql, params);
            return affectedRows;
        } finally {
            long elapsed = watch.elapsed();
            logger.debug("execute, sql={}, params={}, affectedRows={}, elapsed={}", sql, new SQLParams(operation.enumMapper, params), affectedRows, elapsed);
            boolean slow = track(elapsed, 0, affectedRows, 1);
            logQueryPlan(inefficientPlan, slow, sql, params);
        }
    }

    @Override
    public int[] batchExecute(String sql, List<Object[]> params) {
        if (params.isEmpty()) throw new Error("params must not be empty");
        String inefficientPlan = inspector.inspect(sql, params.getFirst());

        var watch = new StopWatch();
        int affectedRows = 0;
        try {
            int[] results = operation.batchUpdate(sql, params);
            for (int result : results) {
                // with batchInsert, mysql returns -2 if insert succeeds, for batch only
                // refer to com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchedInserts
                if (result == Statement.SUCCESS_NO_INFO) affectedRows++;
                if (result > 0) affectedRows += result;
            }
            return results;
        } finally {
            long elapsed = watch.elapsed();
            int size = params.size();
            logger.debug("batchExecute, sql={}, params={}, size={}, affectedRows={}, elapsed={}", sql, new SQLBatchParams(operation.enumMapper, params), size, affectedRows, elapsed);
            boolean slow = track(elapsed, 0, affectedRows, size);
            logQueryPlan(inefficientPlan, slow, sql, params.getFirst());
        }
    }

    <T> RowMapper<T> rowMapper(Class<T> viewClass) {
        @SuppressWarnings("unchecked")
        RowMapper<T> mapper = (RowMapper<T>) rowMappers.get(viewClass);
        if (mapper == null)
            throw new Error("view class is not registered, please register in module by db().view(), viewClass=" + viewClass.getCanonicalName());
        return mapper;
    }

    private <T> void registerViewClass(Class<T> viewClass) {
        if (rowMappers.containsKey(viewClass)) {
            throw new Error("found duplicate view class, viewClass=" + viewClass.getCanonicalName());
        }
        RowMapper<T> mapper = new RowMapperBuilder<>(viewClass, operation.enumMapper).build();
        rowMappers.put(viewClass, mapper);
    }

    // return if slow
    boolean track(long elapsed, int readRows, int writeRows, int queries) {
        ActionLog actionLog = LogManager.currentActionLog();
        if (actionLog != null) {
            actionLog.stats.compute("db_queries", (k, oldValue) -> (oldValue == null) ? queries : oldValue + queries);
            return actionLog.track("db", elapsed, readRows, writeRows, 0, 0);
        }
        return false;
    }

    private void logQueryPlan(@Nullable String inefficientPlan, boolean slow, String sql, Object[] params) {
        if (inefficientPlan != null) {
            logger.warn(errorCode("INEFFICIENT_QUERY"), "inefficient query, plan:\n{}", inefficientPlan);
        } else if (slow) {
            logger.debug("plan:\n{}", inspector.explain(sql, params));
        }
    }
}
