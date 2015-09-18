package core.log.queue;

import core.framework.api.queue.MessageHandler;
import core.framework.api.search.ElasticSearchType;
import core.framework.impl.log.queue.ActionLogMessage;
import core.log.domain.ActionLogDocument;

import javax.inject.Inject;

/**
 * @author neo
 */
public class ActionLogMessageHandler implements MessageHandler<ActionLogMessage> {
    @Inject
    ElasticSearchType<ActionLogDocument> actionType;

    @Override
    public void handle(ActionLogMessage message) throws Exception {
        ActionLogDocument action = new ActionLogDocument();
        action.date = message.date;
        action.app = message.app;
        action.serverIP = message.serverIP;
        action.id = message.id;
        action.result = message.result;
        action.refId = message.refId;
        action.action = message.action;
        action.errorMessage = message.errorMessage;
        action.exceptionClass = message.exceptionClass;
        action.elapsed = message.elapsed;
        action.context = message.context;
        action.performanceStats = message.performanceStats;
        actionType.index(message.id, action);
    }
}
