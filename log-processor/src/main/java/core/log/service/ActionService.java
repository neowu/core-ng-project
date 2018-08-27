package core.log.service;

import core.framework.impl.log.message.ActionLogMessage;
import core.framework.inject.Inject;
import core.framework.search.BulkIndexRequest;
import core.framework.search.ElasticSearchType;
import core.framework.search.IndexRequest;
import core.framework.util.Maps;
import core.log.domain.ActionDocument;
import core.log.domain.TraceDocument;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ActionService {
    @Inject
    IndexService indexService;
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
        request.index = indexService.indexName("action", now);
        request.id = action.id;
        request.source = action;
        actionType.index(request);
    }

    private void indexActions(Map<String, ActionDocument> actions, LocalDate now) {
        BulkIndexRequest<ActionDocument> request = new BulkIndexRequest<>();
        request.index = indexService.indexName("action", now);
        request.sources = actions;
        actionType.bulkIndex(request);
    }

    private void indexTrace(TraceDocument trace, LocalDate now) {
        IndexRequest<TraceDocument> request = new IndexRequest<>();
        request.index = indexService.indexName("trace", now);
        request.id = trace.id;
        request.source = trace;
        traceType.index(request);
    }

    private void indexTraces(Map<String, TraceDocument> traces, LocalDate now) {
        BulkIndexRequest<TraceDocument> request = new BulkIndexRequest<>();
        request.index = indexService.indexName("trace", now);
        request.sources = traces;
        traceType.bulkIndex(request);
    }

    private TraceDocument trace(ActionLogMessage message) {
        var document = new TraceDocument();
        document.date = message.date;
        document.id = message.id;
        document.app = message.app;
        document.action = message.action;
        document.result = message.result;
        document.content = message.traceLog;
        document.errorCode = message.errorCode;
        return document;
    }

    private ActionDocument action(ActionLogMessage message) {
        var document = new ActionDocument();
        document.date = message.date;
        document.app = message.app;
        document.serverIP = message.serverIP;
        document.id = message.id;
        document.result = message.result;
        document.refId = message.refId;
        document.action = message.action;
        document.errorCode = message.errorCode;
        document.errorMessage = message.errorMessage;
        document.elapsed = message.elapsed;
        document.cpuTime = message.cpuTime;
        document.context = message.context;
        document.stats = message.stats;
        document.performanceStats = message.performanceStats;
        return document;
    }
}
