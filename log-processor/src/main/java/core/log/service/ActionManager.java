package core.log.service;

import core.framework.api.search.BulkIndexRequest;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.search.IndexRequest;
import core.framework.api.util.Maps;
import core.framework.impl.log.queue.ActionLogMessage;
import core.log.domain.ActionDocument;
import core.log.domain.TraceDocument;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ActionManager {
    @Inject
    ElasticSearchType<ActionDocument> actionType;
    @Inject
    ElasticSearchType<TraceDocument> traceType;

    public void index(List<ActionLogMessage> messages) {
        LocalDate now = LocalDate.now();
        index(messages, now);
    }

    void index(List<ActionLogMessage> messages, LocalDate now) {
        if (messages.size() <= 5) { // use single index in quiet time
            for (ActionLogMessage message : messages) {
                indexAction(action(message), now);
                if (message.traceLog != null) {
                    indexTrace(trace(message), now);
                }
            }
        } else {
            Map<String, ActionDocument> actions = Maps.newHashMapWithExpectedSize(messages.size());
            Map<String, TraceDocument> traces = Maps.newHashMap();
            for (ActionLogMessage message : messages) {
                actions.put(message.id, action(message));
                if (message.traceLog != null) {
                    traces.put(message.id, trace(message));
                }
            }
            indexActions(actions, now);
            if (!traces.isEmpty()) {
                indexTraces(traces, now);
            }
        }
    }

    private void indexAction(ActionDocument action, LocalDate now) {
        IndexRequest<ActionDocument> request = new IndexRequest<>();
        request.index = IndexName.name("action", now);
        request.id = action.id;
        request.source = action;
        actionType.index(request);
    }

    private void indexActions(Map<String, ActionDocument> actions, LocalDate now) {
        BulkIndexRequest<ActionDocument> request = new BulkIndexRequest<>();
        request.index = IndexName.name("action", now);
        request.sources = actions;
        actionType.bulkIndex(request);
    }

    private void indexTrace(TraceDocument trace, LocalDate now) {
        IndexRequest<TraceDocument> request = new IndexRequest<>();
        request.index = IndexName.name("trace", now);
        request.id = trace.id;
        request.source = trace;
        traceType.index(request);
    }

    private void indexTraces(Map<String, TraceDocument> traces, LocalDate now) {
        BulkIndexRequest<TraceDocument> request = new BulkIndexRequest<>();
        request.index = IndexName.name("trace", now);
        request.sources = traces;
        traceType.bulkIndex(request);
    }

    private TraceDocument trace(ActionLogMessage message) {
        TraceDocument traceLog = new TraceDocument();
        traceLog.date = message.date;
        traceLog.id = message.id;
        traceLog.app = message.app;
        traceLog.action = message.action;
        traceLog.result = message.result;
        traceLog.content = message.traceLog;
        traceLog.errorCode = message.errorCode;
        return traceLog;
    }

    private ActionDocument action(ActionLogMessage message) {
        ActionDocument actionLog = new ActionDocument();
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
