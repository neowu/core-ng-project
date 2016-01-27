package core.log.service;

import core.framework.api.search.BulkIndexRequest;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.Maps;
import core.framework.impl.log.queue.ActionLogMessage;
import core.log.domain.ActionLogDocument;
import core.log.domain.TraceLogDocument;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ActionLogManager {
    @Inject
    ElasticSearchType<ActionLogDocument> actionType;
    @Inject
    ElasticSearchType<TraceLogDocument> traceType;

    public void index(List<ActionLogMessage> logs) {
        LocalDate now = LocalDate.now();
        index(logs, now);
    }

    void index(List<ActionLogMessage> logs, LocalDate now) {
        Map<String, ActionLogDocument> actionLogs = Maps.newHashMapWithExpectedSize(logs.size());
        Map<String, TraceLogDocument> traceLogs = Maps.newHashMap();
        for (ActionLogMessage message : logs) {
            ActionLogDocument actionLog = actionLog(message);
            actionLogs.put(actionLog.id, actionLog);
            if (message.traceLog != null) {
                traceLogs.put(message.id, traceLog(message));
            }
        }
        indexActionLogs(actionLogs, now);
        if (!traceLogs.isEmpty()) {
            indexTraceLogs(traceLogs, now);
        }
    }

    private void indexTraceLogs(Map<String, TraceLogDocument> traceLogs, LocalDate now) {
        BulkIndexRequest<TraceLogDocument> request = new BulkIndexRequest<>();
        request.index = indexName("trace", now);
        request.sources = traceLogs;
        traceType.bulkIndex(request);
    }

    private void indexActionLogs(Map<String, ActionLogDocument> actionLogs, LocalDate now) {
        BulkIndexRequest<ActionLogDocument> request = new BulkIndexRequest<>();
        request.index = indexName("action", now);
        request.sources = actionLogs;
        actionType.bulkIndex(request);
    }

    String indexName(String type, LocalDate now) {
        return type + "-" + now.format(DateTimeFormatter.ISO_DATE);
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
