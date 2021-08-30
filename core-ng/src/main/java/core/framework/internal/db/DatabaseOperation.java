package core.framework.internal.db;

import core.framework.db.UncheckedSQLException;
import core.framework.internal.resource.Pool;
import core.framework.internal.resource.PoolItem;
import core.framework.util.Lists;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class DatabaseOperation {
    public final TransactionManager transactionManager;
    final EnumDBMapper enumMapper = new EnumDBMapper();
    public int batchSize = 1000;   // use 1000 as default batch size by considering actual use cases
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
            return statement.executeUpdate();
        } catch (SQLException e) {
            Connections.checkConnectionState(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    int[] batchUpdate(String sql, List<Object[]> params) {
        int size = params.size();
        int[] results = new int[size];
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            int index = 1;
            for (Object[] batchParams : params) {
                setParams(statement, batchParams);
                statement.addBatch();
                if (index % batchSize == 0 || index == size) {
                    int[] batchResults = statement.executeBatch();
                    System.arraycopy(batchResults, 0, results, index - batchResults.length, batchResults.length);
                }
                index++;
            }
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
        int size = params.size();
        long[] results = generatedColumn == null ? null : new long[size];
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = insertStatement(connection.resource, sql, generatedColumn)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            int index = 1;
            int resultIndex = 0;
            for (Object[] batchParams : params) {
                setParams(statement, batchParams);
                statement.addBatch();
                if (index % batchSize == 0 || index == size) {
                    statement.executeBatch();
                    if (generatedColumn != null) {
                        fetchGeneratedKeys(statement, results, resultIndex);
                        resultIndex = index - 1;
                    }
                }
                index++;
            }
            return generatedColumn != null ? Optional.of(results) : Optional.empty();
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

    private void fetchGeneratedKeys(PreparedStatement statement, long[] results, int resultIndex) throws SQLException {
        int index = resultIndex;
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys != null) {
                while (keys.next()) {
                    results[index++] = keys.getLong(1);
                }
            }
        }
    }

    private void setParams(PreparedStatement statement, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                setParam(statement, i + 1, params[i]);
            }
        }
    }

    private void setParam(PreparedStatement statement, int index, Object param) throws SQLException {
        if (param instanceof String) {
            statement.setString(index, (String) param);
        } else if (param instanceof Integer) {
            statement.setInt(index, (Integer) param);
        } else if (param instanceof Enum) {
            statement.setString(index, enumMapper.getDBValue((Enum<?>) param));
        } else if (param instanceof LocalDateTime) {
            statement.setTimestamp(index, Timestamp.valueOf((LocalDateTime) param));
        } else if (param instanceof ZonedDateTime) {
            // https://dev.mysql.com/doc/refman/8.0/en/datetime.html,
            // TIMESTAMP has a range of '1970-01-01 00:00:01' UTC to '2038-01-19 03:14:07' UTC.
            // for timestamp column type, there is year 2038 issue
            // for datetime column type, jdbc will save UTC values
            statement.setTimestamp(index, Timestamp.from(((ZonedDateTime) param).toInstant()));
        } else if (param instanceof Boolean) {
            statement.setBoolean(index, (Boolean) param);
        } else if (param instanceof Long) {
            statement.setLong(index, (Long) param);
        } else if (param instanceof Double) {
            statement.setDouble(index, (Double) param);
        } else if (param instanceof BigDecimal) {
            statement.setBigDecimal(index, (BigDecimal) param);
        } else if (param instanceof LocalDate) {
            statement.setDate(index, Date.valueOf((LocalDate) param));
        } else if (param == null) {
            statement.setNull(index, Types.NULL);   // both mysql/hsql driver are not using sqlType param
        } else {
            throw new Error(format("unsupported param type, type={}, value={}", param.getClass().getCanonicalName(), param));
        }
    }
}
