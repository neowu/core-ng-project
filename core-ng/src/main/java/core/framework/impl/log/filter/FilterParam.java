package core.framework.impl.log.filter;

import java.util.Set;

/**
 * @author neo
 */
public interface FilterParam {
    String filter(Set<String> maskedFields);
}
