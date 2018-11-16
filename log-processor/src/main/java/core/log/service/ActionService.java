package core.log.service;

import core.framework.inject.Inject;
import core.framework.internal.log.message.ActionLogMessage;
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
        index(messages, LocalDate.now());
    }

    public void index(ActionLogMessage message) {
        index(message, LocalDate.now());
    }

    void index(List<ActionLogMessage> messages, LocalDate now) {
        if (messages.size() <= 5) { // use single index in quiet time
            for (ActionLogMessage message : messages) {
                index(message, now);
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

    private void index(ActionLogMessage message, LocalDate now) {
        indexAction(message.id, action(message), now);
        if (message.traceLog != null) {
            indexTrace(message.id, trace(message), now);
        }
    }

    private void indexAction(String id, ActionDocument action, LocalDate now) {
        IndexRequest<ActionDocument> request = new IndexRequest<>();
        request.index = indexService.indexName("action", now);
        request.id = id;
        request.source = action;
        actionType.index(request);
    }

    private void indexTrace(String id, TraceDocument trace, LocalDate now) {
        IndexRequest<TraceDocument> request = new IndexRequest<>();
        request.index = indexService.indexName("trace", now);
        request.id = id;
        request.source = trace;
        traceType.index(request);
    }

    private void indexActions(Map<String, ActionDocument> actions, LocalDate now) {
        BulkIndexRequest<ActionDocument> request = new BulkIndexRequest<>();
        request.index = indexService.indexName("action", now);
        request.sources = actions;
        actionType.bulkIndex(request);
    }

    private void indexTraces(Map<String, TraceDocument> traces, LocalDate now) {
        BulkIndexRequest<TraceDocument> request = new BulkIndexRequest<>();
        request.index = indexService.indexName("trace", now);
        request.sources = traces;
        traceType.bulkIndex(request);
    }

    private ActionDocument action(ActionLogMessage message) {
        var document = new ActionDocument();
        document.timestamp = message.date;
        document.app = message.app;
        document.serverIP = message.serverIP;
        document.result = message.result;
        document.action = message.action;
        document.refIds = message.refIds;
        document.clients = message.clients;
        document.correlationIds = message.correlationIds;
        document.errorCode = message.errorCode;
        document.errorMessage = message.errorMessage;
        document.elapsed = message.elapsed;
        document.cpuTime = message.cpuTime;
        document.context = message.context;
        document.stats = message.stats;
        document.performanceStats = message.performanceStats;
        return document;
    }

    private TraceDocument trace(ActionLogMessage message) {
        var document = new TraceDocument();
        document.timestamp = message.date;
        document.app = message.app;
        document.action = message.action;
        document.result = message.result;
        document.errorCode = message.errorCode;
        document.content = message.traceLog;
        return document;
    }
}
