package core.framework.internal.log.filter;

import java.util.Set;

/**
 * @author neo
 */
public interface LogParam {
    void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength);
}
