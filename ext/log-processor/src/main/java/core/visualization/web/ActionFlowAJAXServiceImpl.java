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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    public ActionFlowResponseV2 actionFlowV2(String actionId) {
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

//        Set<String> apps = new HashSet<>();
        Map<EdgeKey, EdgeInfo> edges = new HashMap<>();

        for (int i = 0; i < correlationIds.size(); i++) {
            String correlationId = correlationIds.get(i);
            ActionDocument firstAction = correlationId.equals(actionId) ? requestedAction : actionDocument(correlationId).orElse(null);
            if (firstAction != null) {
                graphBuilder.append(format("{} [shape=point];\n", format(START_POINT, i)));

//                apps.add(firstAction.app);

                Map.Entry<EdgeKey, EdgeInfo> firstEdge = firstEdgeInfo(firstAction, i);
                edges.put(firstEdge.getKey(), firstEdge.getValue());
//            } else {
//                apps.add(OUTSIDE_APP);
            }

            List<ActionDocument> actions = searchActionDocument(correlationId);
            Map<String, ActionDocument> actionMap = actions.stream().collect(Collectors.toMap(action -> action.id, action -> action));
            actionMap.put(correlationId, firstAction);

            for (ActionDocument action : actions) {
//                apps.add(action.app);

                Map<EdgeKey, EdgeInfo> edgeInfos = edgeInfo(action, actionMap);
                edgeInfos.forEach((infoKey, infoValue) -> edges.compute(infoKey, (key, value) -> value == null ? infoValue : mergedEdgeInfo(value, infoValue)));
            }
        }

//        for (String app : apps) {
//            graphBuilder.append(format("{} [];\n", nodeName(app)));
//        }
        for (Map.Entry<EdgeKey, EdgeInfo> edge : edges.entrySet()) {
            String edgeStyle = edgeStyle(edge.getValue().actionType);
            String edgeColor = edgeColor(edge.getValue().errors);
            int arrowSize = arrowSize(edge.getValue().largestElapsedInNS);
            int penwidth = Math.min(edge.getValue().count, 3);
            graphBuilder.append(format("{} -> {} [id=\"{}\", arrowhead=open, arrowtail=none, style={}, color={}, arrowsize={}, penwidth={}, fontsize=10, label=\"{}\"];\n",
                nodeName(edge.getKey().srcApp), nodeName(edge.getKey().destApp), format(EDGE_ID, edge.getValue().actionIdWithLargestElapsed), edgeStyle, edgeColor, arrowSize, penwidth, edge.getKey().action));

            response.edges.add(edge(edge.getValue()));
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

    private Map.Entry<EdgeKey, EdgeInfo> firstEdgeInfo(ActionDocument action, int i) {
        String startPoint = format(START_POINT, i);
        EdgeKey edgeKey = new EdgeKey(startPoint, action.app, action.action);

        EdgeInfo edgeInfo = new EdgeInfo();
        edgeInfo.controller = controller(action);
        edgeInfo.actionType = actionType(action);
        edgeInfo.count = 1;
        edgeInfo.actionIdWithLargestElapsed = action.id;
        edgeInfo.largestElapsedInNS = action.elapsed;

        if (!"OK".equals(action.result)) {
            ActionError error = new ActionError();
            error.result = action.result;
            error.errorCode = action.errorCode;
            error.errorMessage = action.errorMessage;
            edgeInfo.errors.add(error);
        }

        return Map.entry(edgeKey, edgeInfo);
    }

    private Map<EdgeKey, EdgeInfo> edgeInfo(ActionDocument action, Map<String, ActionDocument> actionMap) {
        Map<String, Integer> refs = refCount(action, actionMap);

        Map<EdgeKey, EdgeInfo> edgeInfos = new HashMap<>(refs.size());
        for (Map.Entry<String, Integer> ref : refs.entrySet()) {
            EdgeKey edgeKey = new EdgeKey(ref.getKey(), action.app, action.action);

            EdgeInfo edgeInfo = new EdgeInfo();
            edgeInfo.controller = controller(action);
            edgeInfo.actionType = actionType(action);
            edgeInfo.count = ref.getValue();
            edgeInfo.actionIdWithLargestElapsed = action.id;
            edgeInfo.largestElapsedInNS = action.elapsed;

            if (!"OK".equals(action.result)) {
                ActionError error = new ActionError();
                error.result = action.result;
                error.errorCode = action.errorCode;
                error.errorMessage = action.errorMessage;
                edgeInfo.errors.add(error);
            }

            edgeInfos.put(edgeKey, edgeInfo);
        }
        return edgeInfos;
    }

    private Map<String, Integer> refCount(ActionDocument action, Map<String, ActionDocument> actionMap) {
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
        return refs;
    }

    private EdgeInfo mergedEdgeInfo(EdgeInfo originalInfo, EdgeInfo newInfo) {
        if (originalInfo.actionType != newInfo.actionType || !originalInfo.controller.equals(newInfo.controller))
            throw new IllegalArgumentException("controller or actionType not match original EdgeInfo");

        EdgeInfo mergedEdgeInfo = new EdgeInfo();
        mergedEdgeInfo.controller = originalInfo.controller;
        mergedEdgeInfo.actionType = originalInfo.actionType;
        mergedEdgeInfo.count = originalInfo.count + newInfo.count;
        if (originalInfo.largestElapsedInNS > newInfo.largestElapsedInNS) {
            mergedEdgeInfo.actionIdWithLargestElapsed = originalInfo.actionIdWithLargestElapsed;
            mergedEdgeInfo.largestElapsedInNS = originalInfo.largestElapsedInNS;
        } else {
            mergedEdgeInfo.actionIdWithLargestElapsed = newInfo.actionIdWithLargestElapsed;
            mergedEdgeInfo.largestElapsedInNS = newInfo.largestElapsedInNS;
        }
        if (!newInfo.errors.isEmpty())
            mergedEdgeInfo.errors.addAll(newInfo.errors);

        return mergedEdgeInfo;
    }

    private ActionFlowResponse.Edge edge(EdgeInfo edgeInfo) {
        ActionFlowResponse.Edge edge = new ActionFlowResponse.Edge();
        edge.id = format(EDGE_ID, edgeInfo.actionIdWithLargestElapsed);
        edge.html = tooltipHtml(edgeInfo);
        return edge;
    }

    private String tooltipHtml(EdgeInfo edgeInfo) {
        var htmlBuilder = new StringBuilder(100);
        htmlBuilder.append(edgeInfo.controller)
                   .append("<br/>count: ").append(edgeInfo.count)
                   .append("<br/>largestElapsed: ").append(BigDecimal.valueOf(edgeInfo.largestElapsedInNS).divide(ELAPSED_TO_SECOND_DIVISOR));
        for (ActionError error : edgeInfo.errors) {
            htmlBuilder.append("<br/>errorCode: ").append(error.errorCode)
                       .append("<br/>errorMessage: ").append(error.errorMessage);
        }
        return htmlBuilder.toString();
    }

    private String nodeName(String app) {
        return app.replaceAll("-", "_");
    }

    private String edgeStyle(ActionFlowAJAXServiceImpl.ActionType actionType) {
        if (actionType == ActionFlowAJAXServiceImpl.ActionType.HANDLER || actionType == ActionFlowAJAXServiceImpl.ActionType.EXECUTOR)
            return "dashed";
        return "solid";
    }

    private String edgeColor(List<ActionError> errors) {
        if (errors.isEmpty()) return "black";

        for (ActionError error : errors) {
            if ("ERROR".equals(error.result))
                return "red";
        }
        return "orange";
    }

    private int arrowSize(long largestElapsed) {
        if (largestElapsed > 30000000000L)
            return 3;
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

    private String controller(ActionDocument action) {
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

    private static class EdgeInfo {
        String controller;
        ActionType actionType;
        int count;
        String actionIdWithLargestElapsed;
        long largestElapsedInNS;
        List<ActionError> errors = new ArrayList<>();
    }

    private static final class EdgeKey {
        final String srcApp;
        final String destApp;
        final String action;

        private EdgeKey(String srcApp, String destApp, String action) {
            this.srcApp = srcApp;
            this.destApp = destApp;
            this.action = action;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            EdgeKey edgeKey = (EdgeKey) object;
            return Objects.equals(srcApp, edgeKey.srcApp)
                && Objects.equals(destApp, edgeKey.destApp)
                && Objects.equals(action, edgeKey.action);
        }

        @Override
        public int hashCode() {
            return Objects.hash(srcApp, destApp, action);
        }
    }

    private static class ActionError {
        String result;
        String errorCode;
        String errorMessage;
    }
}
