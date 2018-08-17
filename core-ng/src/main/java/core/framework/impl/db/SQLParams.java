package core.framework.impl.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class SQLParams {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLParams.class);
    private final EnumDBMapper mapper;
    private final Object[] params;

    SQLParams(EnumDBMapper mapper, Object... params) {
        this.mapper = mapper;
        this.params = params;
    }

    @Override
    public String toString() {
        if (params == null) return "null";
        var builder = new StringBuilder().append('[');
        int length = params.length;
        for (int i = 0; i < length; i++) {
            if (i > 0) builder.append(", ");
            builder.append(value(params[i]));
        }
        return builder.append(']').toString();
    }

    private String value(Object param) {
        if (param instanceof Enum) {
            try {
                return mapper.getDBValue((Enum<?>) param);
            } catch (Throwable e) {
                LOGGER.warn("failed to get db enum value, error={}", e.getMessage(), e);
                return String.valueOf(param);
            }
        }
        return String.valueOf(param);
    }
}
