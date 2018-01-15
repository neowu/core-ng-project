package core.framework.impl.db;

import core.framework.db.Database;
import core.framework.db.Repository;
import core.framework.db.Transaction;
import core.framework.db.UncheckedSQLException;
import core.framework.impl.resource.Pool;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * @author neo
 */
public final class DatabaseImpl implements Database {
    static final RowMapper.IntegerRowMapper ROW_MAPPER_INTEGER = new RowMapper.IntegerRowMapper();

    public final Pool<Connection> pool;
    public final DatabaseOperation operation;

    private final Logger logger = LoggerFactory.getLogger(DatabaseImpl.class);
    private final Map<Class<?>, RowMapper<?>> rowMappers = Maps.newHashMap();
    public int tooManyRowsReturnedThreshold = 1000;
    public String user;
    public String password;
    public Vendor vendor;
    long slowOperationThresholdInNanos = Duration.ofSeconds(5).toNanos();
    private String url;
    private Properties driverProperties;
    private Duration timeout;
    private Driver driver;

    public DatabaseImpl(String name) {
        initializeRowMappers();

        pool = new Pool<>(this::createConnection, name);
        pool.size(5, 50);    // default optimization for AWS medium/large instances
        pool.maxIdleTime = Duration.ofHours(2);  // make sure db server does not kill connection shorter than this, e.g. MySQL default wait_timeout is 8 hours

        operation = new DatabaseOperation(pool);
        timeout(Duration.ofSeconds(15));
    }

    private void initializeRowMappers() {
        rowMappers.put(String.class, new RowMapper.StringRowMapper());
        rowMappers.put(Integer.class, ROW_MAPPER_INTEGER);
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
            driverProperties = driverProperties();
            this.driverProperties = driverProperties;
        }
        try {
            return driver.connect(url, driverProperties);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    private Properties driverProperties() {
        Properties properties = new Properties();
        if (user != null) properties.put("user", user);
        if (password != null) properties.put("password", password);
        String timeoutValue = String.valueOf(timeout.toMillis());
        if (url.startsWith("jdbc:mysql:")) {
            properties.put("connectTimeout", timeoutValue);
            properties.put("socketTimeout", timeoutValue);
        } else if (url.startsWith("jdbc:oracle:")) {
            properties.put("oracle.net.CONNECT_TIMEOUT", timeoutValue);
            properties.put("oracle.jdbc.ReadTimeout", timeoutValue);
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
        if (!url.startsWith("jdbc:")) throw Exceptions.error("jdbc url must start with \"jdbc:\", url={}", url);
        logger.info("set database connection url, url={}", url);
        this.url = url;
        driver = driver(url);
    }

    private Driver driver(String url) {
        if (url.startsWith("jdbc:mysql:")) {
            return createDriver("com.mysql.cj.jdbc.Driver");
        } else if (url.startsWith("jdbc:oracle:")) {
            return createDriver("oracle.jdbc.OracleDriver");
        } else if (url.startsWith("jdbc:hsqldb:")) {
            return createDriver("org.hsqldb.jdbc.JDBCDriver");
        } else {
            throw Exceptions.error("not supported database, url={}", url);
        }
    }

    private Driver createDriver(String driverClass) {
        try {
            return (Driver) Class.forName(driverClass).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public void slowOperationThreshold(Duration threshold) {
        slowOperationThresholdInNanos = threshold.toNanos();
    }

    public <T> void view(Class<T> viewClass) {
        StopWatch watch = new StopWatch();
        try {
            new DatabaseClassValidator(viewClass).validateViewClass();
            registerViewClass(viewClass);
        } finally {
            logger.info("register db view, viewClass={}, elapsedTime={}", viewClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        StopWatch watch = new StopWatch();
        try {
            new DatabaseClassValidator(entityClass).validateEntityClass();
            RowMapper<T> mapper = registerViewClass(entityClass);
            return new RepositoryImpl<>(this, entityClass, mapper);
        } finally {
            logger.info("register db entity, entityClass={}, elapsedTime={}", entityClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    @Override
    public Transaction beginTransaction() {
        return operation.transactionManager.beginTransaction();
    }

    @Override
    public <T> List<T> select(String sql, Class<T> viewClass, Object... params) {
        StopWatch watch = new StopWatch();
        int returnedRows = 0;
        try {
            List<T> results = operation.select(sql, rowMapper(viewClass), params);
            returnedRows = results.size();
            checkTooManyRowsReturned(returnedRows);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, returnedRows, 0);
            logger.debug("select, sql={}, params={}, updatedRows={}, elapsedTime={}", sql, params, returnedRows, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public <T> Optional<T> selectOne(String sql, Class<T> viewClass, Object... params) {
        StopWatch watch = new StopWatch();
        try {
            return operation.selectOne(sql, rowMapper(viewClass), params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, 1, 0);
            logger.debug("selectOne, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public int execute(String sql, Object... params) {
        StopWatch watch = new StopWatch();
        int updatedRows = 0;
        try {
            updatedRows = operation.update(sql, params);
            return updatedRows;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime, 0, updatedRows);
            logger.debug("execute, sql={}, params={}, updatedRows={}, elapsedTime={}", sql, params, updatedRows, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private <T> RowMapper<T> rowMapper(Class<T> viewClass) {
        @SuppressWarnings("unchecked")
        RowMapper<T> mapper = (RowMapper<T>) rowMappers.get(viewClass);
        if (mapper == null)
            throw Exceptions.error("view class is not registered, please register in module by db().view(), viewClass={}", viewClass.getCanonicalName());
        return mapper;
    }

    private <T> RowMapper<T> registerViewClass(Class<T> viewClass) {
        if (rowMappers.containsKey(viewClass)) {
            throw Exceptions.error("found duplicate view class, viewClass={}", viewClass.getCanonicalName());
        }
        RowMapper<T> mapper = new RowMapperBuilder<>(viewClass, operation.enumMapper).build();
        rowMappers.put(viewClass, mapper);
        return mapper;
    }

    private void checkTooManyRowsReturned(int size) {
        if (size > tooManyRowsReturnedThreshold) {
            logger.warn(Markers.errorCode("TOO_MANY_ROWS_RETURNED"), "too many rows returned, returnedRows={}", size);
        }
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_DB"), "slow db operation, elapsedTime={}", elapsedTime);
        }
    }
}
