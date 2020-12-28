package core.framework.internal.db;

import core.framework.internal.log.filter.LogParam;

import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
class SQLBatchParams implements LogParam {
    private final EnumDBMapper mapper;
    private final List<Object[]> params;

    SQLBatchParams(EnumDBMapper mapper, List<Object[]> params) {
        this.mapper = mapper;
        this.params = params;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        int maxLength = builder.length() + maxParamLength;
        builder.append('[');
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

            if (builder.length() > maxLength) {
                builder.setLength(maxLength);
                builder.append("...(truncated)");
                return;
            }

            index++;
        }
        builder.append(']');
    }
}
