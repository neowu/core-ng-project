package core.diagram.web;

import core.diagram.service.Diagram;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static core.framework.util.Strings.format;

/**
 * @author allison
 */
public class DiagramAJAXServiceImplV2 {
    private static final String ACTION_INDEX = "action-*";
    private static final String START_POINT = "p{}";
    private static final String OUTSIDE_APP = "OUTSIDE";
    private static final String EDGE_ID = "id_{}";
    private static final BigDecimal ELAPSED_TO_SECOND_DIVISOR = BigDecimal.valueOf(1000000000);
    private static final String HTML_LINE_BREAK = "<br/>{}";

    @Inject
    ElasticSearchType<ActionDocument> actionType;

//    public DiagramResponse actionFlowV1(String actionId) {
//        final DiagramResponse responseV1 = new DiagramResponse();
//        responseV1.tooltips = List.of();
//        responseV1.graph = """
//                digraph G {
//                rankdir=LR;
//                p0 [shape=point];
//                transaction_created [shape=box, color=orange];
//                order_settled [shape=box, color=orange];
//                update_customer_index_request [shape=box, color=orange];
//                auto_bet_order_updated [shape=box, color=orange];
//                periodic_draw_result_event [shape=box, color=orange];
//                generate_draw_results_request [shape=box, color=orange];
//                order_updated [shape=box, color=orange];
//                relay_draw_result_request [shape=box, color=orange];
//                wallet_updated [shape=box, color=orange];
//                profit_mode_items_created [shape=box, color=orange];
//                process_pending_auto_bet_order_request [shape=box, color=orange];
//                process_pending_order_request [shape=box, color=orange];
//                winning_summary [shape=box, color=orange];
//                wallet_service -> transaction_created [id="id_77A952ECEC2309209FB0", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=3, fontsize=10, ];
//                transaction_created -> wallet_service [id="id_77A952ECEC2309209FB0", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=3, fontsize=10, ];
//                order_service -> order_settled [id="id_77A952ECE333D2897890", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
//                order_settled -> order_service [id="id_77A952ECE333D2897890", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
//                customer_service -> update_customer_index_request [id="id_77A952F128342C442847", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=3, fontsize=10, ];
//                update_customer_index_request -> customer_service [id="id_77A952F128342C442847", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=3, fontsize=10, ];
//                profit_service -> customer_service [id="id_77A952EDCD342C44282E", arrowhead=open, arrowtail=none, style=solid, color=black, arrowsize=1, penwidth=2, fontsize=10, label="api:get:/customer/:customerId/parent-agents"];
//                order_settled -> statistics_service [id="id_77A952ECE4CE8E4A1885", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
//                order_settled -> draw_service [id="id_77A952ECE35D8CE9643A", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
//                chat_service -> order_service [id="id_77A953003833D28978B5", arrowhead=open, arrowtail=none, style=solid, color=black, arrowsize=1, penwidth=2, fontsize=10, label="api:put:/order"];
//                order_service -> auto_bet_order_updated [id="id_77A952ED0333D289789A", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                auto_bet_order_updated -> order_service [id="id_77A952ED0333D289789A", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                draw_service -> periodic_draw_result_event [id="id_77A952ECAD33D2897882", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                periodic_draw_result_event -> order_service [id="id_77A952ECAD33D2897882", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                scheduler_service -> generate_draw_results_request [id="id_77A952EC955D8CE96431", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                generate_draw_results_request -> draw_service [id="id_77A952EC955D8CE96431", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                order_service -> order_updated [id="id_77A952ECE433D2897891", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                order_updated -> order_service [id="id_77A952ECE433D2897891", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                draw_service -> relay_draw_result_request [id="id_77A952EC9E5D8CE96435", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                relay_draw_result_request -> draw_service [id="id_77A952EC9E5D8CE96435", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                chat_service -> chat_service [id="id_77A952ECB025BDAB4A01", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
//                wallet_service -> wallet_updated [id="id_77A952EDE1CE8E4A1896", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=3, fontsize=10];
//                wallet_updated -> statistics_service [id="id_77A952EDE1CE8E4A1896", arrowhead=normal, arrowtail=none, style=dotted, color=black, arrowsize=0.5, penwidth=0.8, fontsize=10];
//                profit_service -> wallet_service [id="id_77A952EDD22309209FBE", arrowhead=open, arrowtail=none, style=solid, color=black, arrowsize=1, penwidth=1, fontsize=10, label="api:post:/wallet/:customerId"];
//                statistics_service -> customer_service [id="id_77A952EDE4342C44282F", arrowhead=open, arrowtail=none, style=solid, color=black, arrowsize=1, penwidth=1, fontsize=10, label="api:get:/customer/:customerId"];
//                order_service -> profit_mode_items_created [id="id_77A952ED025D8CE9643E", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                profit_mode_items_created -> draw_service [id="id_77A952ED025D8CE9643E", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                p0 -> scheduler_service [id="id_77A952EC81C1795C22DC", arrowhead=open, arrowtail=none, style=solid, color=black, arrowsize=1, penwidth=1, fontsize=10, label="job:f1ssc-generate-draw-result-job"];
//                periodic_draw_result_event -> chat_service [id="id_77A952ECAF25BDAB49FF", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                order_service -> wallet_service [id="id_77A952ECC22309209FAB", arrowhead=open, arrowtail=none, style=solid, color=black, arrowsize=1, penwidth=2, fontsize=10, label="api:post:/wallet/:customerId"];
//                order_service -> process_pending_auto_bet_order_request [id="id_77A952ECBF33D2897888", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                process_pending_auto_bet_order_request -> order_service [id="id_77A952ECBF33D2897888", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                order_settled -> profit_service [id="id_77A952EDBE901D2000A7", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
//                periodic_draw_result_event -> chat [id="id_77A952ECAD46DB32C60F", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                order_service -> wallet_service [id="id_77A952ECE72309209FAF", arrowhead=open, arrowtail=none, style=solid, color=black, arrowsize=1, penwidth=1, fontsize=10, label="api:post:/wallet/:customerId/freeze"];
//                wallet_updated -> customer_service [id="id_77A952F0ED342C442841", arrowhead=normal, arrowtail=none, style=dotted, color=black, arrowsize=0.5, penwidth=0.8, fontsize=10];
//                order_settled -> campaign_service [id="id_77A952ECEC5A15F8A768", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
//                order_service -> process_pending_order_request [id="id_77A952ECB533D2897886", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                process_pending_order_request -> order_service [id="id_77A952ECB533D2897886", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=1, fontsize=10, ];
//                chat_service -> winning_summary [id="id_77A953004546DB32C626", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
//                winning_summary -> chat [id="id_77A953004546DB32C626", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
//                wallet_updated -> profit_service [id="id_77A952EF12901D2000B1", arrowhead=normal, arrowtail=none, style=dotted, color=black, arrowsize=0.5, penwidth=0.8, fontsize=10];
//                }""";
//        return responseV1;
//    }

    public Diagram actionFlowV2(String actionId) {
        ActionDocument requestedAction = actionDocument(actionId).orElseThrow(() -> new NotFoundException("action not found, id=" + actionId));
        boolean isFirstAction = requestedAction.correlationIds == null || requestedAction.correlationIds.isEmpty();

        StringBuilder graphBuilder = new StringBuilder();
        graphBuilder.append("digraph G {\n");

        var response = new Diagram();

        List<String> correlationIds = isFirstAction ? List.of(actionId) : requestedAction.correlationIds;

//        Set<String> apps = new HashSet<>();
        for (int i = 0; i < correlationIds.size(); i++) {
            String correlationId = correlationIds.get(i);
            ActionDocument firstAction = correlationId.equals(actionId) ? requestedAction : actionDocument(correlationId).orElse(null);
            if (firstAction != null) {
//                apps.add(firstAction.app);

                Edge edge = firstEdge(firstAction, i);
                graphBuilder.append(edge.edgeGraph);
                response.tooltips.addAll(edge.tooltipInfo);
//            } else {
//                apps.add(OUTSIDE_APP);
            }


            List<ActionDocument> actions = searchActionDocument(correlationId);
            Map<String, ActionDocument> actionMap = actions.stream().collect(Collectors.toMap(action -> action.id, action -> action));
            actionMap.put(correlationId, firstAction);
            for (ActionDocument action : actions) {
//                apps.add(action.app);

                Edge edge = edge(action, actionMap);
                graphBuilder.append(edge.edgeGraph);
                response.tooltips.addAll(edge.tooltipInfo);
            }
        }

//        for (String app : apps) {
//            graphBuilder.append(format("{} [];\n", nodeName(app)));
//        }

        graphBuilder.append('}');

        response.dot = graphBuilder.toString();
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
        request.limit = 10000;
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
        edge.tooltipInfo.add(edgeInfo(edgeId, action));
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

            edge.tooltipInfo.add(edgeInfo(edgeId, action));
            i++;
        }

        edge.edgeGraph = edgeBuilder.toString();
        return edge;
    }

    private String edgeStyle(ActionDocument action) {
        DiagramAJAXServiceImplV2.ActionType actionType = actionType(action);
        if (actionType == DiagramAJAXServiceImplV2.ActionType.HANDLER || actionType == DiagramAJAXServiceImplV2.ActionType.EXECUTOR)
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

    private DiagramAJAXServiceImplV2.ActionType actionType(ActionDocument action) {
        if (action.context.get("controller") != null)
            return DiagramAJAXServiceImplV2.ActionType.CONTROLLER;
        if (action.context.get("handler") != null)
            return DiagramAJAXServiceImplV2.ActionType.HANDLER;
        if (action.context.get("job_class") != null)
            return DiagramAJAXServiceImplV2.ActionType.JOB_CLASS;
        if (action.context.get("root_action") != null)
            return DiagramAJAXServiceImplV2.ActionType.EXECUTOR;
        if ("app:start".equals(action.action))
            return DiagramAJAXServiceImplV2.ActionType.APP_START;
        if ("app:stop".equals(action.action))
            return DiagramAJAXServiceImplV2.ActionType.APP_STOP;
        else
            throw new IllegalArgumentException("cannot determine action Type");
    }

    private String nodeName(String app) {
        return app.replaceAll("-", "_");
    }

    private Diagram.Tooltip edgeInfo(String edgeId, ActionDocument action) {
        var edge = new Diagram.Tooltip();
        edge.id = edgeId;

        var htmlBuilder = new StringBuilder(100);
        htmlBuilder.append(actionName(action));
        if (action.elapsed != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "elapsed: ")).append(BigDecimal.valueOf(action.elapsed).divide(ELAPSED_TO_SECOND_DIVISOR));
        if (action.stats.get("cpu_time") != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "cpuTime: ")).append(BigDecimal.valueOf(action.stats.get("cpu_time").longValue()).divide(ELAPSED_TO_SECOND_DIVISOR));
        if (action.performanceStats.get("http") != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "httpElapsed: ")).append(BigDecimal.valueOf(action.performanceStats.get("http").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR));
        if (action.performanceStats.get("db") != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "dbElapsed: ")).append(BigDecimal.valueOf(action.performanceStats.get("db").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR));
        if (action.performanceStats.get("redis") != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "redisElapsed: ")).append(BigDecimal.valueOf(action.performanceStats.get("redis").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR));
        if (action.performanceStats.get("elasticsearch") != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "esElapsed: ")).append(BigDecimal.valueOf(action.performanceStats.get("elasticsearch").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR));
        if (action.performanceStats.get("kafka") != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "kafkaElapsed: ")).append(BigDecimal.valueOf(action.performanceStats.get("kafka").totalElapsed).divide(ELAPSED_TO_SECOND_DIVISOR));
        if (action.stats.get("cache_hits") != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "cacheHits: ")).append(action.stats.get("cache_hits").intValue());
        if (action.errorCode != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "errorCode: ")).append(action.errorCode);
        if (action.errorMessage != null)
            htmlBuilder.append(format(HTML_LINE_BREAK, "errorMessage: ")).append(action.errorMessage);

        edge.html = htmlBuilder.toString();
        return edge;
    }

    private String actionName(ActionDocument action) {
        return switch (actionType(action)) {
            case CONTROLLER -> action.context.get("controller").get(0);
            case HANDLER -> action.context.get("handler").get(0);
            case JOB_CLASS -> action.context.get("job_class").get(0);
            case EXECUTOR -> "Executor";
            case APP_START -> "app:start";
            case APP_STOP -> "app:stop";
        };
    }

    private enum ActionType {
        CONTROLLER, HANDLER, JOB_CLASS, EXECUTOR, APP_START, APP_STOP
    }

    private static class Edge {
        String edgeGraph;
        List<Diagram.Tooltip> tooltipInfo = new ArrayList<>();
    }
}
