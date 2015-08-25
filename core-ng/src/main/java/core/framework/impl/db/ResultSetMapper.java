package core.framework.impl.db;

import core.framework.api.db.Row;
import core.framework.api.db.RowMapper;
import core.framework.api.db.UncheckedSQLException;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ResultSetMapper implements Row {
    private final ResultSet resultSet;

    // JDBC ResultSet doesn't support to ignore non-existed column, this to build index
    private final Map<String, Integer> columnIndex;

    public ResultSetMapper(ResultSet resultSet) {
        this.resultSet = resultSet;
        try {
            columnIndex = buildIndex();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public <T> List<T> map(RowMapper<T> rowMapper) {
        try {
            List<T> results = Lists.newArrayList();
            while (resultSet.next()) {
                T result = rowMapper.map(this);
                results.add(result);
            }
            return results;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    private Integer index(String column) {
        return columnIndex.get(column.toLowerCase());
    }

    private Map<String, Integer> buildIndex() throws SQLException {
        Map<String, Integer> index = new HashMap<>();
        ResultSetMetaData meta = resultSet.getMetaData();
        int count = meta.getColumnCount();
        for (int i = 1; i < count + 1; i++) {
            String column = meta.getColumnLabel(i);
            index.put(column.toLowerCase(), i);
        }
        return index;
    }

    @Override
    public Integer getInt(String column) {
        Integer index = index(column);
        if (index == null) return null;
        return getInt(index);
    }

    @Override
    public Integer getInt(int columnIndex) {
        try {
            int value = resultSet.getInt(columnIndex);
            if (resultSet.wasNull()) return null;
            return value;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public Boolean getBoolean(String column) {
        Integer index = index(column);
        if (index == null) return null;
        return getBoolean(index);
    }

    @Override
    public Boolean getBoolean(int columnIndex) {
        try {
            boolean value = resultSet.getBoolean(columnIndex);
            if (resultSet.wasNull()) return null;
            return value;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public Long getLong(String column) {
        Integer index = index(column);
        if (index == null) return null;
        return getLong(index);
    }

    @Override
    public Long getLong(int columnIndex) {
        try {
            long value = resultSet.getLong(columnIndex);
            if (resultSet.wasNull()) return null;
            return value;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public Double getDouble(String column) {
        Integer index = index(column);
        if (index == null) return null;
        return getDouble(index);
    }

    @Override
    public Double getDouble(int columnIndex) {
        try {
            double value = resultSet.getDouble(columnIndex);
            if (resultSet.wasNull()) return null;
            return value;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public String getString(String column) {
        Integer index = index(column);
        if (index == null) return null;
        return getString(index);
    }

    @Override
    public String getString(int columnIndex) {
        try {
            return resultSet.getString(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public BigDecimal getBigDecimal(String column) {
        Integer index = index(column);
        if (index == null) return null;
        return getBigDecimal(index);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) {
        try {
            return resultSet.getBigDecimal(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public LocalDateTime getLocalDateTime(String column) {
        Integer index = index(column);
        if (index == null) return null;
        return getLocalDateTime(index);
    }

    @Override
    public LocalDateTime getLocalDateTime(int columnIndex) {
        try {
            Timestamp timestamp = resultSet.getTimestamp(columnIndex);
            if (timestamp == null) return null;
            Instant instant = timestamp.toInstant();
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public <T extends Enum> T getEnum(String column, Class<T> enumClass) {
        Integer index = index(column);
        if (index == null) return null;
        return getEnum(index, enumClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum> T getEnum(int columnIndex, Class<T> enumClass) {
        try {
            String value = resultSet.getString(columnIndex);
            if (value == null) return null;
            Enum[] enums = enumClass.getEnumConstants();
            for (Enum item : enums) {
                if (String.valueOf(item).equals(value)) return (T) item;
            }
            throw Exceptions.error("can not parse value to enum, enumType={}, value={}", enumClass.getCanonicalName(), value);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }
}
