package core.framework.internal.log.appender;

import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.StatMessage;

/**
 * @author neo
 */
public interface LogAppender {
    void append(ActionLogMessage message);

    void append(StatMessage message);
}
