package core.framework.internal.log;

import core.framework.log.LogAppender;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.StatMessage;

/**
 * @author neo
 */
public class CompositeLogAppender implements LogAppender {
    public LogAppender systemAppender;
    public LogAppender customAppender;

    @Override
    public void append(ActionLogMessage message) {
        if (systemAppender != null) systemAppender.append(message);
        if (customAppender != null) customAppender.append(message);
    }

    @Override
    public void append(StatMessage message) {
        if (systemAppender != null) systemAppender.append(message);
        if (customAppender != null) customAppender.append(message);
    }
}
