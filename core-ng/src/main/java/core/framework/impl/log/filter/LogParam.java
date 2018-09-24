package core.framework.impl.log.filter;

import java.util.Set;

/**
 * @author neo
 */
public interface LogParam {
    String filter(Set<String> maskedFields);
}
