package core.framework.internal.log.filter;

import java.util.Set;

/**
 * @author neo
 */
public class ArrayLogParam implements LogParam {
    private final String[] values;

    public ArrayLogParam(String... values) {
        this.values = values;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        LogParamHelper.append(builder, values, maxParamLength);
    }
}
