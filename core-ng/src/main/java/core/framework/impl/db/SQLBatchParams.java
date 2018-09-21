package core.framework.impl.db;

import java.util.List;

/**
 * @author neo
 */
class SQLBatchParams {
    private final EnumDBMapper mapper;
    private final List<Object[]> params;

    SQLBatchParams(EnumDBMapper mapper, List<Object[]> params) {
        this.mapper = mapper;
        this.params = params;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder().append('[');
        int index = 0;
        for (Object[] batch : params) {
            if (index > 0) builder.append(", ");
            builder.append('[');
            int length = batch.length;
            for (int i = 0; i < length; i++) {
                if (i > 0) builder.append(", ");
                builder.append(SQLParams.value(batch[i], mapper));
            }
            builder.append(']');
            index++;
        }
        return builder.append(']').toString();
    }
}
