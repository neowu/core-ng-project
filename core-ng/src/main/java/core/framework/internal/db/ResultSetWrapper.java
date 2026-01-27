package core.framework.internal.db;

import core.framework.db.Dialect;
import core.framework.db.UncheckedSQLException;
import core.framework.util.ASCII;
import core.framework.util.Maps;
import org.jspecify.annotations.Nullable;

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
import java.util.UUID;

/**
 * @author neo
 */
public final class ResultSetWrapper {
    private final ResultSet resultSet;

    // JDBC ResultSet doesn't support to ignore non-existed column, this to build index
    private final Map<String, Integer> columnIndex;
    private final Dialect dialect;

    ResultSetWrapper(ResultSet resultSet, Dialect dialect) {
        this.resultSet = resultSet;
        try {
            columnIndex = columnIndex();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
        this.dialect = dialect;
    }

    @Nullable
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

    @Nullable
    public Integer getInt(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getInt(index);
    }

    Integer getInt(int index) throws SQLException {
        return resultSet.getObject(index, Integer.class);
    }

    @Nullable
    public Boolean getBoolean(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getBoolean(index);
    }

    Boolean getBoolean(int index) throws SQLException {
        return resultSet.getObject(index, Boolean.class);
    }

    @Nullable
    public Long getLong(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLong(index);
    }

    Long getLong(int index) throws SQLException {
        return resultSet.getObject(index, Long.class);
    }

    @Nullable
    public Double getDouble(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getDouble(index);
    }

    Double getDouble(int index) throws SQLException {
        return resultSet.getObject(index, Double.class);
    }

    @Nullable
    public String getString(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getString(index);
    }

    String getString(int index) throws SQLException {
        return resultSet.getString(index);
    }

    @Nullable
    public BigDecimal getBigDecimal(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getBigDecimal(index);
    }

    BigDecimal getBigDecimal(int index) throws SQLException {
        return resultSet.getBigDecimal(index);
    }

    @Nullable
    public LocalDateTime getLocalDateTime(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLocalDateTime(index);
    }

    LocalDateTime getLocalDateTime(int index) throws SQLException {
        return resultSet.getObject(index, LocalDateTime.class);
    }

    @Nullable
    public LocalDate getLocalDate(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getLocalDate(index);
    }

    LocalDate getLocalDate(int index) throws SQLException {
        return resultSet.getObject(index, LocalDate.class);
    }

    @Nullable
    public ZonedDateTime getZonedDateTime(String column) throws SQLException {
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

    @Nullable
    public UUID getUUID(String column) throws SQLException {
        Integer index = index(column);
        if (index == null) return null;
        return getUUID(index);
    }

    @Nullable
    UUID getUUID(int index) throws SQLException {
        if (dialect == Dialect.MYSQL) {
            String uuid = resultSet.getString(index);
            return uuid != null ? UUID.fromString(uuid) : null;
        } else {
            // both hsqldb and postgres support UUID type
            return resultSet.getObject(index, UUID.class);
        }
    }

}
