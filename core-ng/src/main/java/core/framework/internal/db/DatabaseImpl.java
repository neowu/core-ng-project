package core.framework.internal.db;

import com.mysql.cj.conf.PropertyKey;
import core.framework.db.Database;
import core.framework.db.IsolationLevel;
import core.framework.db.Repository;
import core.framework.db.Transaction;
import core.framework.db.UncheckedSQLException;
import core.framework.internal.resource.Pool;
import core.framework.log.ActionLogContext;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class DatabaseImpl implements Database {
    public final Pool<Connection> pool;
    public final DatabaseOperation operation;

    private final Logger logger = LoggerFactory.getLogger(DatabaseImpl.class);
    private final Map<Class<?>, RowMapper<?>> rowMappers = new HashMap<>(32);
    public String user;
    public String password;
    public int maxOperations = 5000;  // max db calls per action, if exceeds, it indicates either wrong impl (e.g. infinite loop with db calls) or bad practice (not CD friend), better split into multiple actions
    public int tooManyRowsReturnedThreshold = 1000;
    public long slowOperationThresholdInNanos = Duration.ofSeconds(5).toNanos();
    public IsolationLevel isolationLevel;
    private String url;
    private Properties driverProperties;
    private Duration timeout;
    private Driver driver;

    public DatabaseImpl(String name) {
        initializeRowMappers();

        pool = new Pool<>(this::createConnection, name);
        pool.size(5, 50);    // default optimization for AWS medium/large instances
        pool.maxIdleTime = Duration.ofHours(1);  // make sure db server does not kill connection shorter than this, e.g. MySQL default wait_timeout is 8 hours
        pool.validator(connection -> connection.isValid(1), Duration.ofSeconds(30));

        operation = new DatabaseOperation(pool);
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
    }

    private Connection createConnection() {
        if (url == null) throw new Error("url must not be null");
        Properties driverProperties = this.driverProperties;
        if (driverProperties == null) {
            driverProperties = driverProperties(url, user, password);
            this.driverProperties = driverProperties;
        }
        try {
            Connection connection = driver.connect(url, driverProperties);
            if (isolationLevel != null) connection.setTransactionIsolation(isolationLevel.level);
            return connection;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    Properties driverProperties(String url, String user, String password) {
        var properties = new Properties();
        if (user != null) properties.setProperty("user", user);
        if (password != null) properties.setProperty("password", password);
        if (url.startsWith("jdbc:mysql:")) {
            String timeoutValue = String.valueOf(timeout.toMillis());
            // refer to https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
            properties.setProperty(PropertyKey.connectTimeout.getKeyName(), timeoutValue);
            properties.setProperty(PropertyKey.socketTimeout.getKeyName(), timeoutValue);
            properties.setProperty(PropertyKey.rewriteBatchedStatements.getKeyName(), "true");
            properties.setProperty(PropertyKey.queryInterceptors.getKeyName(), MySQLQueryInterceptor.class.getName());
            properties.setProperty(PropertyKey.logger.getKeyName(), "Slf4JLogger");
            int index = url.indexOf('?');
            // mysql with ssl has overhead, usually we ensure security on arch level, e.g. gcloud sql proxy or firewall rule
            if (index == -1 || url.indexOf("useSSL=", index + 1) == -1) properties.setProperty(PropertyKey.useSSL.getKeyName(), "false");
            // refer to https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-charsets.html
            // without characterEncoding, mysql driver auto detects charset based on server side var character_set_server
            // and only character_set_server=utf8mb4 will map characterEncoding to utf-8
            // here use utf-8 as default value, and support to override by jdbc url param
            if (index == -1 || url.indexOf("characterEncoding=", index + 1) == -1) properties.setProperty(PropertyKey.characterEncoding.getKeyName(), "utf-8");
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
        if (!url.startsWith("jdbc:")) throw new Error("jdbc url must start with \"jdbc:\", url=" + url);
        logger.info("set database connection url, url={}", url);
        this.url = url;
        driver = driver(url);
    }

    private Driver driver(String url) {
        if (url.startsWith("jdbc:mysql:")) {
            return createDriver("com.mysql.cj.jdbc.Driver");
        } else if (url.startsWith("jdbc:hsqldb:")) {
            return createDriver("org.hsqldb.jdbc.JDBCDriver");
        } else {
            throw new Error("not supported database, url=" + url);
        }
    }

    private Driver createDriver(String driverClass) {
        try {
            return (Driver) Class.forName(driverClass).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public <T> void view(Class<T> viewClass) {
        var watch = new StopWatch();
        try {
            new DatabaseClassValidator(viewClass).validateViewClass();
            registerViewClass(viewClass);
        } finally {
            logger.info("register db view, viewClass={}, elapsed={}", viewClass.getCanonicalName(), watch.elapsed());
        }
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        var watch = new StopWatch();
        try {
            new DatabaseClassValidator(entityClass).validateEntityClass();
            registerViewClass(entityClass);
            return new RepositoryImpl<>(this, entityClass);
        } finally {
            logger.info("register db entity, entityClass={}, elapsed={}", entityClass.getCanonicalName(), watch.elapsed());
        }
    }

    @Override
    public Transaction beginTransaction() {
        return operation.transactionManager.beginTransaction();
    }

    @Override
    public <T> List<T> select(String sql, Class<T> viewClass, Object... params) {
        var watch = new StopWatch();
        int returnedRows = 0;
        try {
            List<T> results = operation.select(sql, rowMapper(viewClass), params);
            returnedRows = results.size();
            checkTooManyRowsReturned(returnedRows);
            return results;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, returnedRows, 0);
            logger.debug("select, sql={}, params={}, returnedRows={}, elapsed={}", sql, new SQLParams(operation.enumMapper, params), returnedRows, elapsed);
            checkOperation(elapsed, operations);
        }
    }

    @Override
    public <T> Optional<T> selectOne(String sql, Class<T> viewClass, Object... params) {
        var watch = new StopWatch();
        int returnedRows = 0;
        try {
            Optional<T> result = operation.selectOne(sql, rowMapper(viewClass), params);
            if (result.isPresent()) returnedRows = 1;
            return result;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, returnedRows, 0);
            logger.debug("selectOne, sql={}, params={}, returnedRows={}, elapsed={}", sql, new SQLParams(operation.enumMapper, params), returnedRows, elapsed);
            checkOperation(elapsed, operations);
        }
    }

    @Override
    public int execute(String sql, Object... params) {
        var watch = new StopWatch();
        int updatedRows = 0;
        try {
            updatedRows = operation.update(sql, params);
            return updatedRows;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, updatedRows);
            logger.debug("execute, sql={}, params={}, updatedRows={}, elapsed={}", sql, new SQLParams(operation.enumMapper, params), updatedRows, elapsed);
            checkOperation(elapsed, operations);
        }
    }

    @Override
    public int[] batchExecute(String sql, List<Object[]> params) {
        StopWatch watch = new StopWatch();
        int updatedRows = 0;
        try {
            int[] results = operation.batchUpdate(sql, params);
            updatedRows = Arrays.stream(results).sum();
            return results;
        } finally {
            long elapsed = watch.elapsed();
            int operations = ActionLogContext.track("db", elapsed, 0, updatedRows);
            logger.debug("batchExecute, sql={}, params={}, size={}, updatedRows={}, elapsed={}", sql, new SQLBatchParams(operation.enumMapper, params), params.size(), updatedRows, elapsed);
            checkOperation(elapsed, operations);
        }
    }

    private <T> RowMapper<T> rowMapper(Class<T> viewClass) {
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

    private void checkTooManyRowsReturned(int size) {
        if (size > tooManyRowsReturnedThreshold) {
            logger.warn(errorCode("TOO_MANY_ROWS_RETURNED"), "too many rows returned, returnedRows={}", size);
        }
    }

    void checkOperation(long elapsed, int operations) {
        if (operations > maxOperations) {
            throw new Error("too many db operations, operations=" + operations);
        }
        if (elapsed > slowOperationThresholdInNanos) {
            logger.warn(errorCode("SLOW_DB"), "slow db operation, elapsed={}", elapsed);
        }
    }
}
