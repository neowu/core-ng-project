package core.framework.impl.log;

import core.framework.impl.log.filter.LogFilter;

/**
 * @author neo
 */
public interface Appender {
    void append(ActionLog log, LogFilter filter);
}
