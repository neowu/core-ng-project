package core.framework.impl.db;

import core.framework.impl.log.filter.LogParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author neo
 */
class SQLParams implements LogParam {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLParams.class);

    static String value(Object param, EnumDBMapper mapper) {
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

    private final EnumDBMapper mapper;
    private final Object[] params;

    SQLParams(EnumDBMapper mapper, Object... params) {
        this.mapper = mapper;
        this.params = params;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        if (params == null) {
            builder.append("null");
        } else {
            builder.append('[');
            int length = params.length;
            for (int i = 0; i < length; i++) {
                if (i > 0) builder.append(", ");
                builder.append(value(params[i], mapper));
            }
            builder.append(']');
        }
    }
}
