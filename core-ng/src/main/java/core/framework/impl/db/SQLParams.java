package core.framework.impl.db;

/**
 * @author neo
 */
public class SQLParams {
    private final EnumDBMapper mapper;
    private final Object[] params;

    SQLParams(EnumDBMapper mapper, Object... params) {
        this.mapper = mapper;
        this.params = params;
    }

    @Override
    public String toString() {
        if (params == null) return "null";
        StringBuilder builder = new StringBuilder().append('[');
        int length = params.length;
        for (int i = 0; i < length; i++) {
            if (i > 0) builder.append(", ");
            builder.append(value(params[i]));
        }
        return builder.append(']').toString();
    }

    private String value(Object param) {
        if (param instanceof Enum) {
            return mapper.getDBValue((Enum<?>) param);
        }
        return String.valueOf(param);
    }
}
