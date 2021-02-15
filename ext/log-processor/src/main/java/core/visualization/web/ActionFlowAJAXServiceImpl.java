package core.visualization.web;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.framework.search.SearchRequest;
import core.framework.web.exception.NotFoundException;
import core.log.domain.ActionDocument;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Optional;

import static core.framework.util.Strings.format;

/**
 * @author allison
 */
public class ActionFlowAJAXServiceImpl implements ActionFlowAJAXService {
    private static final String ACTION_INDEX = "action-*";

    @Inject
    ElasticSearchType<ActionDocument> actionType;

    @Override
    public ActionFlowResponse actionFlow(String actionId) {
        ActionDocument requestedAction = actionDocument(actionId);
        boolean isFirstAction = requestedAction.correlationIds == null || requestedAction.correlationIds.isEmpty();

        List<String> correlationIds = isFirstAction ? List.of(actionId) : requestedAction.correlationIds;
        StringBuilder graphBuilder = new StringBuilder();
        graphBuilder.append("digraph G {\n");

        var response = new ActionFlowResponse();

        for (int i = 0; i < correlationIds.size(); i++) {
            String correlationId = correlationIds.get(i);
            ActionDocument firstAction = correlationId.equals(actionId) ? requestedAction : actionDocument(correlationId);
            graphBuilder.append(firstNode(firstAction, actionId.equals(firstAction.id), i));
            graphBuilder.append(firstEdge(firstAction, i));

            response.nodes.add(nodeInfo(firstAction));
            edgeInfo(firstAction).ifPresent(edge -> response.edges.add(edge));

            List<ActionDocument> actionDocuments = searchActionDocument(correlationId);
            for (ActionDocument actionDocument : actionDocuments) {
                graphBuilder.append(node(actionDocument, actionId.equals(actionDocument.id)));
                graphBuilder.append(edge(actionDocument));

                response.nodes.add(nodeInfo(actionDocument));
                edgeInfo(actionDocument).ifPresent(edge -> response.edges.add(edge));
            }
        }

        graphBuilder.append('}');

        response.graph = graphBuilder.toString();
        return response;
    }

    private ActionDocument actionDocument(String id) {
        var request = new GetRequest();
        request.index = ACTION_INDEX;
        request.id = id;
        return actionType.get(request).orElseThrow(() -> new NotFoundException("action not found, id=" + id));
    }

    private List<ActionDocument> searchActionDocument(String correlationId) {
        var request = new SearchRequest();
        request.query = QueryBuilders.matchQuery("correlation_id", correlationId);
        request.index = ACTION_INDEX;
        return actionType.search(request).hits;
    }

    private ActionFlowResponse.Node nodeInfo(ActionDocument actionDocument) {
        var node = new ActionFlowResponse.Node();
        node.id = actionDocument.id;
        node.cpuTime = actionDocument.stats.get("cpu_time").longValue();
        return node;
    }

    private Optional<ActionFlowResponse.Edge> edgeInfo(ActionDocument actionDocument) {
        if (actionDocument.errorCode == null && actionDocument.errorMessage == null)
            return Optional.empty();

        var edge = new ActionFlowResponse.Edge();
        edge.errorCode = actionDocument.errorCode;
        edge.errorMessage = actionDocument.errorMessage;
        return Optional.of(edge);
    }

    private ActionType actionType(ActionDocument actionDocument) {
        if (actionDocument.context.get("controller") != null)
            return ActionType.HANDLER;
        if (actionDocument.context.get("handler") != null)
            return ActionType.HANDLER;
        if (actionDocument.context.get("job_class") != null)
            return ActionType.JOB_CLASS;
        if (actionDocument.context.get("root_action") != null)
            return ActionType.EXECUTOR;
        else
            throw new IllegalArgumentException("cannot determine action Type");
    }

    private String firstNode(ActionDocument actionDocument, boolean isRequestedAction, int i) {
        String startPoint = "p" + i;
        return format("{} [shape=point];\n", startPoint) + node(actionDocument, isRequestedAction);
    }

    private String node(ActionDocument actionDocument, boolean isRequestedAction) {
        String shape = nodeShape(actionDocument);
        String color = nodeColor(actionDocument, isRequestedAction);
        String content = nodeContent(actionDocument);
        return format("{} [id={}, shape={}, color={}, label=\"{}\\n{}\"];\n", actionDocument.id, actionDocument.id, actionDocument.app, shape, color, content);
    }

    private String nodeShape(ActionDocument actionDocument) {
        return switch (actionType(actionDocument)) {
            case CONTROLLER -> "box";
            case HANDLER -> "hexagon";
            case JOB_CLASS -> "parallelogram";
            case EXECUTOR -> "ellipse";
        };
    }

    private String nodeContent(ActionDocument actionDocument) {
        return switch (actionType(actionDocument)) {
            case CONTROLLER -> actionDocument.context.get("controller").get(0);
            case HANDLER -> actionDocument.context.get("handler").get(0);
            case JOB_CLASS -> actionDocument.context.get("job_class").get(0);
            case EXECUTOR -> "Executor";
        };
    }

    private String nodeColor(ActionDocument actionDocument, boolean isRequestedAction) {
        if (actionDocument.stats.get("cpu_time") > 30000000)
            return "red";
        if (isRequestedAction)
            return "blue";
        return "black";
    }

    private String firstEdge(ActionDocument actionDocument, int i) {
        String startPoint = "p" + i;
        String edgeStyle = edgeStyle(actionDocument);
        String edgeColor = edgeColor(actionDocument);
        return format("{} -> {} [arrowhead=open, arrowtail=none, style={}, color={}, fontsize=10, label=\"{}\"];\n", startPoint, actionDocument.id, edgeStyle, edgeColor, actionDocument.action);
    }

    private String edge(ActionDocument actionDocument) {
        StringBuilder edgeBuilder = new StringBuilder();
        for (String refId : actionDocument.refIds) {
            String edgeStyle = edgeStyle(actionDocument);
            String edgeColor = edgeColor(actionDocument);
            edgeBuilder.append(format("{} -> {} [arrowhead=open, arrowtail=none, style={}, color={}, fontsize=10, label=\"{}\"];\n", refId, actionDocument.id, edgeStyle, edgeColor, actionDocument.action));
        }
        return edgeBuilder.toString();
    }

    private String edgeStyle(ActionDocument actionDocument) {
        ActionType actionType = actionType(actionDocument);
        if (actionType == ActionType.HANDLER || actionType == ActionType.EXECUTOR)
            return "dashed";
        if (actionDocument.elapsed > 30000000)
            return "bold";
        return "solid";
    }

    private String edgeColor(ActionDocument actionDocument) {
        if ("ERROR".equals(actionDocument.result))
            return "red";
        if ("WARN".equals(actionDocument.result))
            return "orange";
        return "black";
    }

    private enum ActionType {
        CONTROLLER, HANDLER, JOB_CLASS, EXECUTOR
    }
}
