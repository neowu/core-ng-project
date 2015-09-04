package core.framework.impl.db;

import core.framework.api.util.Exceptions;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author neo
 */
final class PreparedStatements {
    static void setParams(PreparedStatement statement, Object[] params, EnumDBMapper mapper) throws SQLException {
        int index = 1;
        if (params != null) {
            for (Object param : params) {
                setParam(statement, index, param, mapper);
                index++;
            }
        }
    }

    private static void setParam(PreparedStatement statement, int index, Object param, EnumDBMapper mapper) throws SQLException {
        if (param instanceof String) {
            statement.setString(index, (String) param);
        } else if (param instanceof Integer) {
            statement.setInt(index, (Integer) param);
        } else if (param instanceof Enum) {
            statement.setString(index, mapper.getDBValue((Enum) param));
        } else if (param instanceof LocalDateTime) {
            Timestamp value = Timestamp.from(((LocalDateTime) param).atZone(ZoneId.systemDefault()).toInstant());
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
            throw Exceptions.error("unsupported param type, please contact arch team, param={}", param);
        }
    }
}
