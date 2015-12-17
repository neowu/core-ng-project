package core.log.queue;

import core.framework.api.queue.MessageHandler;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.Maps;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.ActionLogMessages;
import core.log.domain.ActionLogDocument;
import core.log.domain.TraceLogDocument;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author neo
 */
public class ActionLogMessagesHandler implements MessageHandler<ActionLogMessages> {
    @Inject
    ElasticSearchType<ActionLogDocument> actionType;
    @Inject
    ElasticSearchType<TraceLogDocument> traceType;

    @Override
    public void handle(ActionLogMessages messages) throws Exception {
        Map<String, ActionLogDocument> actionLogs = Maps.newHashMapWithExpectedSize(messages.logs.size());
        Map<String, TraceLogDocument> traceLogs = Maps.newHashMap();
        for (ActionLogMessage message : messages.logs) {
            ActionLogDocument actionLog = actionLog(message);
            actionLogs.put(actionLog.id, actionLog);
            if (message.traceLog != null) {
                traceLogs.put(message.id, traceLog(message));
            }
        }
        actionType.bulkIndex(actionLogs);
        if (!traceLogs.isEmpty()) traceType.bulkIndex(traceLogs);
    }

    private TraceLogDocument traceLog(ActionLogMessage message) {
        TraceLogDocument traceLog = new TraceLogDocument();
        traceLog.date = message.date;
        traceLog.id = message.id;
        traceLog.app = message.app;
        traceLog.action = message.action;
        traceLog.result = message.result;
        traceLog.content = message.traceLog;
        return traceLog;
    }

    private ActionLogDocument actionLog(ActionLogMessage message) {
        ActionLogDocument actionLog = new ActionLogDocument();
        actionLog.date = message.date;
        actionLog.app = message.app;
        actionLog.serverIP = message.serverIP;
        actionLog.id = message.id;
        actionLog.result = message.result;
        actionLog.refId = message.refId;
        actionLog.action = message.action;
        actionLog.errorCode = message.errorCode;
        actionLog.errorMessage = message.errorMessage;
        actionLog.elapsed = message.elapsed;
        actionLog.context = message.context;
        actionLog.performanceStats = message.performanceStats;
        return actionLog;
    }
}
