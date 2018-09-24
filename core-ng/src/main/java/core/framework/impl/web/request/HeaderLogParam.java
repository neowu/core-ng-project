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
    public String filter(Set<String> maskedFields) {
        if (maskedFields.contains(header.toString())) return "******";
        if (values.size() == 1) return values.getFirst();
        return Arrays.toString(values.toArray());
    }
}
