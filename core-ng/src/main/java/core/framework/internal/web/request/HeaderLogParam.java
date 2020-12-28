package core.framework.internal.web.request;

import core.framework.internal.log.filter.LogParam;
import core.framework.internal.log.filter.LogParamHelper;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;

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
            String value = values.getFirst();
            LogParamHelper.append(builder, value, maxParamLength);
        } else {
            LogParamHelper.append(builder, values.toArray(), maxParamLength);
        }
    }
}
