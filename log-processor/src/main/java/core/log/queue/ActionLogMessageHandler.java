package core.log.queue;

import core.framework.api.queue.MessageHandler;
import core.framework.api.search.ElasticSearchType;
import core.framework.impl.log.queue.ActionLogMessage;

import javax.inject.Inject;

/**
 * @author neo
 */
public class ActionLogMessageHandler implements MessageHandler<ActionLogMessage> {
    @Inject
    ElasticSearchType<ActionLogMessage> actionType;

    @Override
    public void handle(ActionLogMessage message) throws Exception {
        actionType.index(message.id, message);
    }
}
