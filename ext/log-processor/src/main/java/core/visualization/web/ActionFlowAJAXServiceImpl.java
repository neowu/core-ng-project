package core.visualization.web;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchRequest;
import core.framework.web.exception.NotFoundException;
import core.log.domain.ActionDocument;
import org.elasticsearch.index.query.QueryBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static core.framework.util.Strings.format;

/**
 * @author allison
 */
public class ActionFlowAJAXServiceImpl implements ActionFlowAJAXService {
    private static final String ACTION_INDEX = "action-*";
    private static final String START_POINT = "p{}";
    private static final String OUTSIDE_APP = "OUTSIDE";
    private static final String EDGE_ID = "id_{}";
    private static final BigDecimal ELAPSED_TO_SECOND_DIVISOR = BigDecimal.valueOf(1000000000);

    @Inject
    ElasticSearchType<ActionDocument> actionType;

    @Override
    public ActionFlowResponseV1 actionFlowV1(String actionId) {
        return null;
    }

    @Override
    public ActionFlowResponse actionFlow(String actionId) {
        ActionDocument requestedAction = actionDocument(actionId).orElseThrow(() -> new NotFoundException("action not found, id=" + actionId));
        boolean isFirstAction = requestedAction.correlationIds == null || requestedAction.correlationIds.isEmpty();

        StringBuilder graphBuilder = new StringBuilder();
        graphBuilder.append("digraph G {\n");

        var response = new ActionFlowResponse();

        List<String> correlationIds = isFirstAction ? List.of(actionId) : requestedAction.correlationIds;

        Set<String> apps = new HashSet<>();
        for (int i = 0; i < correlationIds.size(); i++) {
            String correlationId = correlationIds.get(i);
            ActionDocument firstAction = correlationId.equals(actionId) ? requestedAction : actionDocument(correlationId).orElse(null);
            if (firstAction == null)
                apps.add(OUTSIDE_APP);
            else {
                apps.add(firstAction.app);

                Edge edge = firstEdge(firstAction, i);
                graphBuilder.append(edge.edgeGraph);
                response.edges.addAll(edge.edgeInfo);
            }

            List<ActionDocument> actions = searchActionDocument(correlationId);
            Map<String, ActionDocument> actionMap = actions.stream().collect(Collectors.toMap(action -> action.id, action -> action));
            actionMap.put(correlationId, firstAction);
            for (ActionDocument action : actions) {
                apps.add(action.app);

                Edge edge = edge(action, actionMap);
                graphBuilder.append(edge.edgeGraph);
                response.edges.addAll(edge.edgeInfo);
            }
        }

        for (String app : apps) {
            graphBuilder.append(format("{} [];\n", nodeName(app)));
        }

        graphBuilder.append('}');

        response.graph = graphBuilder.toString();
        return response;
    }

    private Optional<ActionDocument> actionDocument(String id) {
        var request = new SearchRequest();
        request.index = ACTION_INDEX;
        request.query = QueryBuilders.idsQuery().addIds(id);
        request.limit = 1;
        List<ActionDocument> documents = actionType.search(request).hits;
        if (documents.isEmpty()) return Optional.empty();
        return Optional.ofNullable(documents.get(0));
    }

    private List<ActionDocument> searchActionDocument(String correlationId) {
        var request = new SearchRequest();
        request.index = ACTION_INDEX;
        request.query = QueryBuilders.matchQuery("correlation_id", correlationId);
        request.limit = 1000;
        return actionType.search(request).hits;
    }

    private Edge firstEdge(ActionDocument action, int i) {
        String startPoint = format(START_POINT, i);
        String edgeId = format(EDGE_ID, action.id);
        String edgeStyle = edgeStyle(action);
        String edgeColor = edgeColor(action);
        int arrowSize = arrowSize(action);

        Edge edge = new Edge();
        edge.edgeGraph = format("{} [shape=point];\n{} -> {} [id=\"{}\", arrowhead=open, arrowtail=none, style={}, color={}, arrowsize={}, fontsize=10, label=\"{}\"];\n", startPoint, startPoint, nodeName(action.app), edgeId, edgeStyle, edgeColor, arrowSize, action.action);
        edge.edgeInfo.add(edgeInfo(edgeId, action));
        return edge;
    }

    private Edge edge(ActionDocument action, Map<String, ActionDocument> actionMap) {
        Edge edge = new Edge();
        StringBuilder edgeBuilder = new StringBuilder();
        Map<String, Integer> refs = new HashMap<>();

        for (String refId : action.refIds) {
            ActionDocument refAction = actionMap.get(refId);
            if (refAction == null) {
                actionDocument(refId).ifPresentOrElse(
                    ref -> refs.compute(ref.app, (key, value) -> value == null ? 1 : value + 1),
                    () -> refs.compute(OUTSIDE_APP, (key, value) -> value == null ? 1 : value + 1));
            } else {
                refs.compute(refAction.app, (key, value) -> value == null ? 1 : value + 1);
            }
        }

        int i = 0;
        for (Map.Entry<String, Integer> ref : refs.entrySet()) {
            String actionId = refs.size() == 1 ? action.id : action.id + "_" + i;
            String edgeId = format(EDGE_ID, actionId);
            String edgeStyle = edgeStyle(action);
            String edgeColor = edgeColor(action);
            int arrowSize = arrowSize(action);
            edgeBuilder.append(format("{} -> {} [id=\"{}\", arrowhead=open, arrowtail=none, style={}, color={}, arrowsize={}, penwidth={}, fontsize=10, label=\"{}\"];\n", nodeName(ref.getKey()), nodeName(action.app), edgeId, edgeStyle, edgeColor, arrowSize, ref.getValue(), action.action));

            edge.edgeInfo.add(edgeInfo(edgeId, action));
            i++;
        }

        edge.edgeGraph = edgeBuilder.toString();
        return edge;
    }

    private String edgeStyle(ActionDocument action) {
        ActionFlowAJAXServiceImpl.ActionType actionType = actionType(action);
        if (actionType == ActionFlowAJAXServiceImpl.ActionType.HANDLER || actionType == ActionFlowAJAXServiceImpl.ActionType.EXECUTOR)
            return "dashed";
        if (action.elapsed > 30000000000L)
            return "bold";
        return "solid";
    }

    private String edgeColor(ActionDocument action) {
        if ("ERROR".equals(action.result))
            return "red";
        if ("WARN".equals(action.result))
            return "orange";
        return "black";
    }

    private int arrowSize(ActionDocument action) {
        if (action.elapsed > 30000000000L)
            return 2;
        return 1;
    }

    private ActionFlowAJAXServiceImpl.ActionType actionType(ActionDocument action) {
        if (action.context.get("controller") != null)
            return ActionFlowAJAXServiceImpl.ActionType.CONTROLLER;
        if (action.context.get("handler") != null)
            return ActionFlowAJAXServiceImpl.ActionType.HANDLER;
        if (action.context.get("job_class") != null)
            return ActionFlowAJAXServiceImpl.ActionType.JOB_CLASS;
        if (action.context.get("root_action") != null)
            return ActionFlowAJAXServiceImpl.ActionType.EXECUTOR;
        else
            throw new IllegalArgumentException("cannot determine action Type");
    }

    private String nodeName(String app) {
        return app.replaceAll("-", "_");
    }

    private ActionFlowResponse.EdgeInfo edgeInfo(String edgeId, ActionDocument action) {
        var edge = new ActionFlowResponse.EdgeInfo();
        edge.id = edgeId;
        edge.actionName = actionName(action);
        edge.elapsed = BigDecimal.valueOf(action.elapsed).divide(ELAPSED_TO_SECOND_DIVISOR);
        edge.errorCode = action.errorCode;
        edge.errorMessage = action.errorMessage;
        edge.cpuTime = BigDecimal.valueOf(action.stats.get("cpu_time").longValue()).divide(ELAPSED_TO_SECOND_DIVISOR);
        edge.httpElapsed = action.performanceStats.get("http") != null ? BigDecimal.valueOf(action.performanceStats.get("http").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR) : null;
        edge.dbElapsed = action.performanceStats.get("db") != null ? BigDecimal.valueOf(action.performanceStats.get("db").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR) : null;
        edge.redisElapsed = action.performanceStats.get("redis") != null ? BigDecimal.valueOf(action.performanceStats.get("redis").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR) : null;
        edge.esElapsed = action.performanceStats.get("elasticsearch") != null ? BigDecimal.valueOf(action.performanceStats.get("elasticsearch").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR) : null;
        edge.kafkaElapsed = action.performanceStats.get("kafka") != null ? BigDecimal.valueOf(action.performanceStats.get("kafka").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR) : null;
        edge.cacheHits = action.stats.get("cache_hits") != null ? action.stats.get("cache_hits").intValue() : null;
        return edge;
    }

    private String actionName(ActionDocument action) {
        return switch (actionType(action)) {
            case CONTROLLER -> action.context.get("controller").get(0);
            case HANDLER -> action.context.get("handler").get(0);
            case JOB_CLASS -> action.context.get("job_class").get(0);
            case EXECUTOR -> "Executor";
        };
    }

    private enum ActionType {
        CONTROLLER, HANDLER, JOB_CLASS, EXECUTOR
    }

    private static class Edge {
        String edgeGraph;
        List<ActionFlowResponse.EdgeInfo> edgeInfo = new ArrayList<>();
    }
}
