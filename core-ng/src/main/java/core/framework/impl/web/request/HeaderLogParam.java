package core.framework.impl.web.request;

import core.framework.impl.log.filter.LogParam;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;

import java.util.Arrays;
import java.util.Set;

/**
 * @author neo
 */
class HeaderLogParam implements LogParam {
    private final HttpString header;
    private final HeaderValues values;

    HeaderLogParam(HttpString header, HeaderValues values) {
        this.header = header;
        this.values = values;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        if (maskedFields.contains(header.toString())) {
            builder.append("******");
        } else if (values.size() == 1) {
            builder.append(values.getFirst());
        } else {
            builder.append(Arrays.toString(values.toArray()));
        }
    }
}
