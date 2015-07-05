package core.framework.api.db;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author neo
 */
public interface Row {
    Integer getInt(String column);

    Integer getInt(int columnIndex);

    Long getLong(String column);

    Long getLong(int columnIndex);

    Double getDouble(String column);

    Double getDouble(int columnIndex);

    BigDecimal getBigDecimal(String column);

    BigDecimal getBigDecimal(int columnIndex);

    Boolean getBoolean(String column);

    Boolean getBoolean(int columnIndex);

    String getString(String column);

    String getString(int columnIndex);

    LocalDateTime getLocalDateTime(String column);

    LocalDateTime getLocalDateTime(int columnIndex);

    <T extends Enum> T getEnum(String column, Class<T> enumClass);

    @SuppressWarnings("unchecked")
    <T extends Enum> T getEnum(int columnIndex, Class<T> enumClass);
}
