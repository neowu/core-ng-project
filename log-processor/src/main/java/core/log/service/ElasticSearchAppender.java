package core.log.service;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.Appender;
import core.framework.impl.log.MessageFactory;
import core.framework.impl.log.filter.LogFilter;
import core.framework.impl.log.message.ActionLogMessage;
import core.framework.inject.Inject;

import java.util.List;

/**
 * @author neo
 */
public class ElasticSearchAppender implements Appender {
    @Inject
    ActionService actionService;

    @Override
    public void append(ActionLog log, LogFilter filter) {
        ActionLogMessage message = MessageFactory.actionLog(log, filter);
        actionService.index(List.of(message));
    }
}
