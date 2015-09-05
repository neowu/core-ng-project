package core.framework.impl.db;

import core.framework.api.db.UncheckedSQLException;
import core.framework.api.util.Maps;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * @author neo
 */
final class ResultSetWrapper {
    private final ResultSet resultSet;

    // JDBC ResultSet doesn't support to ignore non-existed column, this to build index
    private final Map<String, Integer> columnIndex;

    ResultSetWrapper(ResultSet resultSet) {
        this.resultSet = resultSet;
        try {
            columnIndex = buildIndex();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    private Integer index(String column) {
        return columnIndex.get(column.toLowerCase());
    }

    private Map<String, Integer> buildIndex() throws SQLException {
        Map<String, Integer> index = Maps.newHashMap();
        ResultSetMetaData meta = resultSet.getMetaData();
        int count = meta.getColumnCount();
        for (int i = 1; i < count + 1; i++) {
            String column = meta.getColumnLabel(i);
            index.put(column.toLowerCase(), i);
        }
        return index;
    }

    public Integer getInt(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getInt(index);
    }

    public Integer getInt(int columnIndex) throws SQLException {
        int value = resultSet.getInt(columnIndex);
        if (resultSet.wasNull()) return null;
        return value;
    }

    public Boolean getBoolean(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getBoolean(index);
    }

    public Boolean getBoolean(int columnIndex) throws SQLException {
        boolean value = resultSet.getBoolean(columnIndex);
        if (resultSet.wasNull()) return null;
        return value;
    }

    public Long getLong(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLong(index);
    }

    public Long getLong(int columnIndex) throws SQLException {
        long value = resultSet.getLong(columnIndex);
        if (resultSet.wasNull()) return null;
        return value;
    }

    public Double getDouble(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getDouble(index);
    }

    public Double getDouble(int columnIndex) throws SQLException {
        double value = resultSet.getDouble(columnIndex);
        if (resultSet.wasNull()) return null;
        return value;
    }

    public String getString(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getString(index);
    }

    public String getString(int columnIndex) throws SQLException {
        return resultSet.getString(columnIndex);
    }

    public BigDecimal getBigDecimal(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getBigDecimal(index);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return resultSet.getBigDecimal(columnIndex);
    }

    public LocalDateTime getLocalDateTime(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLocalDateTime(index);
    }

    public LocalDateTime getLocalDateTime(int columnIndex) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnIndex);
        if (timestamp == null) return null;
        Instant instant = timestamp.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
