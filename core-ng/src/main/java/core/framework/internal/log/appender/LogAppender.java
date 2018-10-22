package core.framework.internal.log.appender;

import core.framework.internal.log.message.ActionLogMessage;
import core.framework.internal.log.message.StatMessage;

/**
 * @author neo
 */
public interface LogAppender {
    void append(ActionLogMessage message);

    void append(StatMessage message);
}
