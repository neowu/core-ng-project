package core.log.kafka;

import core.framework.impl.log.message.ActionLogMessage;
import core.framework.inject.Inject;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.Message;
import core.log.service.ActionService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class ActionLogMessageHandler implements BulkMessageHandler<ActionLogMessage> {
    @Inject
    ActionService actionService;

    @Override
    public void handle(List<Message<ActionLogMessage>> messages) {
        List<ActionLogMessage> actionLogs = new ArrayList<>(messages.size());
        for (Message<ActionLogMessage> message : messages) {
            actionLogs.add(message.value);
        }
        actionService.index(actionLogs);
    }
}
