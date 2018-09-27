package core.log.service;

import core.framework.impl.log.ActionLog;
import core.framework.impl.log.MessageFactory;
import core.framework.impl.log.message.ActionLogMessage;
import core.framework.inject.Inject;

import java.util.function.Consumer;

/**
 * @author neo
 */
public class ElasticSearchAppender implements Consumer<ActionLog> {
    @Inject
    ActionService actionService;

    @Override
    public void accept(ActionLog log) {
        ActionLogMessage message = MessageFactory.actionLog(log);
        actionService.index(message);
    }
}
