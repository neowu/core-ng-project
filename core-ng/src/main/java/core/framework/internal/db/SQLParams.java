package core.framework.internal.db;

import core.framework.internal.log.filter.LogParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author neo
 */
class SQLParams implements LogParam {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLParams.class);

    static String value(Object param, EnumDBMapper mapper, int maxLength) {
        if (param instanceof Enum) {
            try {
                return mapper.getDBValue((Enum<?>) param);
            } catch (Throwable e) {
                LOGGER.warn("failed to get db enum value, error={}", e.getMessage(), e);
                return String.valueOf(param);
            }
        }
        String value = String.valueOf(param);
        if (value.length() > maxLength) {
            return value.substring(0, maxLength) + "...(truncated)";
        }
        return value;
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
                // roughly limit each string values to 1000 chars,
                // can only break limit when there are 10 more long string params, which is very rare
                builder.append(value(params[i], mapper, maxParamLength / 10));
            }
            builder.append(']');
        }
    }
}
