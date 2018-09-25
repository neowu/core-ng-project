package core.framework.impl.log.filter;

import java.util.Set;

/**
 * @author neo
 */
public interface LogParam {
    int MAX_PARAM_LENGTH = 10000; // limit long param string to 10k

    void append(StringBuilder builder, Set<String> maskedFields);
}
