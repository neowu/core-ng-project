package core.framework.internal.db;

import core.framework.db.QueryDiagnostic;
import core.framework.db.UncheckedSQLException;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.resource.Pool;
import core.framework.internal.resource.PoolItem;
import core.framework.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static core.framework.log.Markers.errorCode;
import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class DatabaseOperation {
    private final Logger logger = LoggerFactory.getLogger(DatabaseOperation.class);

    public final TransactionManager transactionManager;
    final EnumDBMapper enumMapper = new EnumDBMapper();
    int queryTimeoutInSeconds;

    DatabaseOperation(Pool<Connection> pool) {
        transactionManager = new TransactionManager(pool);
    }

    // as for the boilerplate code, it is mainly for performance and maintainability purpose, as framework code it's more important to keep straightforward than DRY
    // it's harder to trace and read if creating a lot of lambda or template pattern, also impact the mem usage and GC
    int update(String sql, Object... params) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            setParams(statement, params);
            int result = statement.executeUpdate();
            logSlowQuery(statement);
            return result;
        } catch (SQLException e) {
            Connections.checkConnectionState(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    // mysql jdbc driver will adjust batch size according to max_allowed_packet param, check this value by "SHOW VARIABLES LIKE '%max_allowed_packet'"
    // refer to com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchedInserts, com.mysql.cj.AbstractPreparedQuery.computeBatchSize
    int[] batchUpdate(String sql, List<Object[]> params) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            for (Object[] batchParams : params) {
                setParams(statement, batchParams);
                statement.addBatch();
            }
            int[] results = statement.executeBatch();
            logSlowQuery(statement);
            return results;
        } catch (SQLException e) {
            Connections.checkConnectionState(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    <T> Optional<T> selectOne(String sql, RowMapper<T> mapper, Object... params) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            setParams(statement, params);
            return fetchOne(statement, mapper);
        } catch (SQLException e) {
            Connections.checkConnectionState(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    <T> List<T> select(String sql, RowMapper<T> mapper, Object... params) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            setParams(statement, params);
            return fetch(statement, mapper);
        } catch (SQLException e) {
            Connections.checkConnectionState(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    OptionalLong insert(String sql, Object[] params, String generatedColumn) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = insertStatement(connection.resource, sql, generatedColumn)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            setParams(statement, params);
            statement.executeUpdate();
            if (generatedColumn == null) return OptionalLong.empty();
            return fetchGeneratedKey(statement);
        } catch (SQLException e) {
            Connections.checkConnectionState(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    Optional<long[]> batchInsert(String sql, List<Object[]> params, String generatedColumn) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = insertStatement(connection.resource, sql, generatedColumn)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            for (Object[] batchParams : params) {
                setParams(statement, batchParams);
                statement.addBatch();
            }
            statement.executeBatch();
            if (generatedColumn != null) {
                long[] results = fetchGeneratedKeys(statement, params.size());
                return Optional.of(results);
            }
            return Optional.empty();
        } catch (SQLException e) {
            Connections.checkConnectionState(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    private PreparedStatement insertStatement(Connection connection, String sql, String generatedColumn) throws SQLException {
        if (generatedColumn == null) return connection.prepareStatement(sql);
        return connection.prepareStatement(sql, new String[]{generatedColumn});
    }

    private <T> Optional<T> fetchOne(PreparedStatement statement, RowMapper<T> mapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            logSlowQuery(statement);

            T result = null;
            if (resultSet.next()) {
                result = mapper.map(new ResultSetWrapper(resultSet));
                if (resultSet.next())
                    throw new Error("more than one row returned");
            }
            return Optional.ofNullable(result);
        }
    }

    private <T> List<T> fetch(PreparedStatement statement, RowMapper<T> mapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            logSlowQuery(statement);

            var wrapper = new ResultSetWrapper(resultSet);
            List<T> results = Lists.newArrayList();
            while (resultSet.next()) {
                T result = mapper.map(wrapper);
                results.add(result);
            }
            return results;
        }
    }

    // the LAST_INSERT_ID() function of mysql returns BIGINT, so here it uses Long
    // http://dev.mysql.com/doc/refman/5.7/en/information-functions.html
    private OptionalLong fetchGeneratedKey(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys != null && keys.next()) {
                return OptionalLong.of(keys.getLong(1));
            }
        }
        return OptionalLong.empty();
    }

    private long[] fetchGeneratedKeys(PreparedStatement statement, int size) throws SQLException {
        long[] results = new long[size];
        int index = 0;
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys != null) {
                while (keys.next()) {
                    results[index++] = keys.getLong(1);
                }
            }
        }
        return results;
    }

    private void setParams(PreparedStatement statement, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                setParam(statement, i + 1, params[i]);
            }
        }
    }

    private void setParam(PreparedStatement statement, int index, Object param) throws SQLException {
        switch (param) {
            case String value -> statement.setString(index, value);
            case Enum<?> value -> statement.setString(index, enumMapper.getDBValue(value));
            case LocalDate value -> statement.setObject(index, value, Types.DATE);
            case LocalDateTime value -> statement.setObject(index, value, Types.TIMESTAMP);
            case ZonedDateTime value -> {
                // https://dev.mysql.com/doc/refman/8.0/en/datetime.html,
                // TIMESTAMP has a range of '1970-01-01 00:00:01' UTC to '2038-01-19 03:14:07' UTC.
                // for timestamp column type, there is year 2038 issue
                // for datetime column type, jdbc will save UTC values

                // with insert ignore, out of range timestamp param will be converted to "0000-00-00 00:00:00" into db, and will trigger "Zero date value prohibited" error on read
                // refer to https://dev.mysql.com/doc/refman/8.0/en/insert.html
                // Data conversions that would trigger errors abort the statement if IGNORE is not specified. With IGNORE, invalid values are adjusted to the closest values and inserted; warnings are produced but the statement does not abort.

                // here only to check > 0, make trade off between validating TIMESTAMP column type and keeping compatible with DATETIME column type
                // most likely the values we deal with from external systems are lesser (e.g. nodejs default year is 1900, it converts 0 into 1900/01/01 00:00:00)
                // if it passes timestamp after 2038-01-19 03:14:07 (Instant.ofEpochSecond(Integer.MAX_VALUE)), it will still trigger this issue on MySQL
                // so on application level, if you can not ensure the range of input value, write its own utils to check
                Instant instant = value.toInstant();
                if (instant.getEpochSecond() <= 0) throw new Error("timestamp must be after 1970-01-01 00:00:00, value=" + param);
                statement.setObject(index, instant, Types.TIMESTAMP);
            }
            case Boolean value -> statement.setBoolean(index, value);
            case Integer value -> statement.setInt(index, value);
            case Long value -> statement.setLong(index, value);
            case Double value -> statement.setDouble(index, value);
            case BigDecimal value -> statement.setBigDecimal(index, value);
            case null -> statement.setNull(index, Types.NULL);   // both mysql/hsql driver are not using sqlType param
            default -> throw new Error(format("unsupported param type, type={}, value={}", param.getClass().getCanonicalName(), param));
        }
    }

    void logSlowQuery(PreparedStatement statement) {
        if (statement instanceof QueryDiagnostic diagnostic) {
            boolean noIndexUsed = diagnostic.noIndexUsed();
            boolean badIndexUsed = diagnostic.noGoodIndexUsed();
            if (!noIndexUsed && !badIndexUsed) return;

            ActionLog actionLog = LogManager.CURRENT_ACTION_LOG.get();
            boolean warning = actionLog == null || !actionLog.warningContext.suppressSlowSQLWarning;
            String message = noIndexUsed ? "no index used" : "bad index used";
            String sqlValue = diagnostic.sql();
            if (warning) {
                logger.warn(errorCode("SLOW_SQL"), "{}, sql={}", message, sqlValue);
            } else {
                logger.debug("{}, sql={}", message, sqlValue);
            }
        }
    }
}
