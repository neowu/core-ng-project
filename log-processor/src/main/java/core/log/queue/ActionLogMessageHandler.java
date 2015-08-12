package core.log.queue;

import core.framework.api.queue.MessageHandler;
import core.framework.api.search.ElasticSearch;
import core.framework.impl.log.ActionLogMessage;

import javax.inject.Inject;

/**
 * @author neo
 */
public class ActionLogMessageHandler implements MessageHandler<ActionLogMessage> {
    @Inject
    ElasticSearch elasticSearch;

    @Override
    public void handle(ActionLogMessage message) throws Exception {
        elasticSearch.index("action", message.id, message);
    }
}
