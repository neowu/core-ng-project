package core.framework.impl.db;

import core.framework.db.UncheckedSQLException;
import core.framework.util.ASCII;
import core.framework.util.Maps;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
            columnIndex = columnIndex();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    private Integer index(String column) {
        return columnIndex.get(ASCII.toLowerCase(column));
    }

    // different db are using various of rules to return column name/label, some of reserved case, some does not
    // here we have to make name/column case insensitive for view mapping
    // http://hsqldb.org/doc/guide/databaseobjects-chapt.html#dbc_collations
    private Map<String, Integer> columnIndex() throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int count = meta.getColumnCount();
        Map<String, Integer> index = Maps.newHashMapWithExpectedSize(count);
        for (int i = 1; i < count + 1; i++) {
            String column = meta.getColumnLabel(i);
            index.put(ASCII.toLowerCase(column), i);
        }
        return index;
    }

    int columnCount() {
        return columnIndex.size();
    }

    Integer getInt(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getInt(index);
    }

    Integer getInt(int index) throws SQLException {
        int value = resultSet.getInt(index);
        if (resultSet.wasNull()) return null;
        return value;
    }

    Boolean getBoolean(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getBoolean(index);
    }

    Boolean getBoolean(int index) throws SQLException {
        boolean value = resultSet.getBoolean(index);
        if (resultSet.wasNull()) return null;
        return value;
    }

    Long getLong(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLong(index);
    }

    Long getLong(int index) throws SQLException {
        long value = resultSet.getLong(index);
        if (resultSet.wasNull()) return null;
        return value;
    }

    Double getDouble(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getDouble(index);
    }

    Double getDouble(int index) throws SQLException {
        double value = resultSet.getDouble(index);
        if (resultSet.wasNull()) return null;
        return value;
    }

    String getString(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getString(index);
    }

    String getString(int index) throws SQLException {
        return resultSet.getString(index);
    }

    BigDecimal getBigDecimal(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getBigDecimal(index);
    }

    BigDecimal getBigDecimal(int index) throws SQLException {
        return resultSet.getBigDecimal(index);
    }

    LocalDateTime getLocalDateTime(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLocalDateTime(index);
    }

    LocalDateTime getLocalDateTime(int index) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(index);
        if (timestamp == null) return null;
        return LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }

    LocalDate getLocalDate(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLocalDate(index);
    }

    LocalDate getLocalDate(int index) throws SQLException {
        Date date = resultSet.getDate(index);
        if (date == null) return null;
        return date.toLocalDate();
    }

    ZonedDateTime getZonedDateTime(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getZonedDateTime(index);
    }

    ZonedDateTime getZonedDateTime(int index) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(index);
        if (timestamp == null) return null;
        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }
}
