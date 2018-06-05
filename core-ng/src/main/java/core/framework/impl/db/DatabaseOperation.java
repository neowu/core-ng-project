package core.framework.impl.db;

import core.framework.db.UncheckedSQLException;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import core.framework.util.Exceptions;
import core.framework.util.Lists;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public class DatabaseOperation {
    public final TransactionManager transactionManager;
    final EnumDBMapper enumMapper = new EnumDBMapper();
    public int batchSize = 1000;   // use 1000 as default batch size by considering both MySQL and Oracle
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
            Connections.checkConnectionStatus(connection, e);
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
                    int[] batchResult = statement.executeBatch();
                    System.arraycopy(batchResult, 0, results, index - batchResult.length, batchResult.length);
                }
                index++;
            }
            return results;
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    <T> Optional<T> selectOne(String sql, RowMapper<T> mapper, Object... params) {
        validateSelectSQL(sql);

        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            setParams(statement, params);
            return fetchOne(statement, mapper);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    <T> List<T> select(String sql, RowMapper<T> mapper, Object... params) {
        validateSelectSQL(sql);

        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            setParams(statement, params);
            return fetch(statement, mapper);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    Optional<Long> insert(String sql, Object[] params, String generatedColumn) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = insertStatement(connection.resource, sql, generatedColumn)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            setParams(statement, params);
            statement.executeUpdate();
            if (generatedColumn == null) return Optional.empty();
            return fetchGeneratedKey(statement);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.returnConnection(connection);
        }
    }

    private PreparedStatement insertStatement(Connection connection, String sql, String generatedColumn) throws SQLException {
        if (generatedColumn == null) return connection.prepareStatement(sql);
        return connection.prepareStatement(sql, new String[]{generatedColumn});
    }

    private void validateSelectSQL(String sql) {
        if (sql.contains("*"))
            throw Exceptions.error("sql must not contain wildcard(*), please only select columns needed, sql={}", sql);
    }

    private <T> Optional<T> fetchOne(PreparedStatement statement, RowMapper<T> mapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            ResultSetWrapper wrapper = new ResultSetWrapper(resultSet);
            T result = null;
            if (resultSet.next()) {
                result = mapper.map(wrapper);
                if (resultSet.next())
                    throw new Error("more than one row returned");
            }
            return Optional.ofNullable(result);
        }
    }

    private <T> List<T> fetch(PreparedStatement statement, RowMapper<T> mapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            ResultSetWrapper wrapper = new ResultSetWrapper(resultSet);
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
    private Optional<Long> fetchGeneratedKey(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return Optional.of(keys.getLong(1));
            }
        }
        return Optional.empty();
    }

    private void setParams(PreparedStatement statement, Object... params) throws SQLException {
        if (params != null) {
            int index = 1;
            for (Object param : params) {
                setParam(statement, index, param);
                index++;
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
            statement.setObject(index, null);
        } else {
            throw Exceptions.error("unsupported param type, please contact arch team, type={}, value={}", param.getClass().getCanonicalName(), param);
        }
    }
}
