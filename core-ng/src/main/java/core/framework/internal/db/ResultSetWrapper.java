package core.framework.internal.db;

import core.framework.db.UncheckedSQLException;
import core.framework.util.ASCII;
import core.framework.util.Maps;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
        return resultSet.getObject(index, Integer.class);
    }

    Boolean getBoolean(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getBoolean(index);
    }

    Boolean getBoolean(int index) throws SQLException {
        return resultSet.getObject(index, Boolean.class);
    }

    Long getLong(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLong(index);
    }

    Long getLong(int index) throws SQLException {
        return resultSet.getObject(index, Long.class);
    }

    Double getDouble(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getDouble(index);
    }

    Double getDouble(int index) throws SQLException {
        return resultSet.getObject(index, Double.class);
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
        return resultSet.getObject(index, LocalDateTime.class);
    }

    LocalDate getLocalDate(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLocalDate(index);
    }

    LocalDate getLocalDate(int index) throws SQLException {
        return resultSet.getObject(index, LocalDate.class);
    }

    ZonedDateTime getZonedDateTime(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getZonedDateTime(index);
    }

    ZonedDateTime getZonedDateTime(int index) throws SQLException {
        // in mysql driver, getObject(type) is faster than getTimestamp/getDate due to "synchronized calendar"
        // hsql doesn't support ZonedDateTime, use OffsetDateTime for both mysql and hsql
        OffsetDateTime time = resultSet.getObject(index, OffsetDateTime.class);
        if (time == null) return null;
        return time.atZoneSameInstant(ZoneId.systemDefault());
    }
}
