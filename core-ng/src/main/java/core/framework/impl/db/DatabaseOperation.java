package core.framework.impl.db;

import core.framework.api.db.UncheckedSQLException;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public class DatabaseOperation {
    public final TransactionManager transactionManager;
    final EnumDBMapper enumMapper = new EnumDBMapper();
    int queryTimeoutInSeconds;

    public DatabaseOperation(Pool<Connection> pool) {
        transactionManager = new TransactionManager(pool);
    }

    // for the boilerplate code, it is mainly for performance and clear purpose, as framework code, it's more important than DRY
    // make a lot of lambda and template pattern will make it harder to read and trace, also impact the mem usage and GC
    int update(String sql, Object[] params) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            setParams(statement, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    int[] batchUpdate(String sql, List<Object[]> params) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            for (Object[] batchParams : params) {
                setParams(statement, batchParams);
                statement.addBatch();
            }
            return statement.executeBatch();
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
    }

    <T> Optional<T> selectOne(String sql, RowMapper<T> mapper, Object[] params) {
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
            transactionManager.releaseConnection(connection);
        }
    }

    <T> List<T> select(String sql, RowMapper<T> mapper, Object[] params) {
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
            transactionManager.releaseConnection(connection);
        }
    }

    Optional<Long> insert(String sql, Object[] params) {
        PoolItem<Connection> connection = transactionManager.getConnection();
        try (PreparedStatement statement = connection.resource.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setQueryTimeout(queryTimeoutInSeconds);
            setParams(statement, params);
            statement.executeUpdate();
            return fetchGeneratedKey(statement);
        } catch (SQLException e) {
            Connections.checkConnectionStatus(connection, e);
            throw new UncheckedSQLException(e);
        } finally {
            transactionManager.releaseConnection(connection);
        }
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

    private void setParams(PreparedStatement statement, Object[] params) throws SQLException {
        int index = 1;
        if (params != null) {
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
            statement.setString(index, enumMapper.getDBValue((Enum) param));
        } else if (param instanceof LocalDateTime) {
            Instant instant = ((LocalDateTime) param).atZone(ZoneId.systemDefault()).toInstant();
            Timestamp value = Timestamp.from(instant);
            statement.setTimestamp(index, value);
        } else if (param instanceof Boolean) {
            statement.setBoolean(index, (Boolean) param);
        } else if (param instanceof Long) {
            statement.setLong(index, (Long) param);
        } else if (param instanceof Double) {
            statement.setDouble(index, (Double) param);
        } else if (param instanceof BigDecimal) {
            statement.setBigDecimal(index, (BigDecimal) param);
        } else if (param == null) {
            statement.setObject(index, null);
        } else {
            throw Exceptions.error("unsupported param type, please contact arch team, type={}, value={}", param.getClass().getCanonicalName(), param);
        }
    }
}
