package core.visualization.service;

import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.log.domain.ActionDocument;
import core.visualization.web.DiagramResponse;

import java.util.ArrayList;

/**
 * @author neo
 */
public class ActionFlowService {
    @Inject
    ElasticSearchType<ActionDocument> actionType;

    public DiagramResponse overall() {
        var response = new DiagramResponse();
        response.graph = """
                digraph G {
                order_settled [shape=box, color=orange];
                wallet_updated [shape=box, color=orange];
                customer_updated [shape=box, color=orange];
                website -> customer_service [id="edge_1", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=3, fontsize=10, ];
                customer_updated -> wallet_service [id="edge_2", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=3, fontsize=10, ];
                order_service -> order_settled [id="edge_3", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
                order_settled -> order_service [id="edge_4", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=2, fontsize=10, ];
                customer_service -> customer_updated [id="edge_5", arrowhead=open, arrowtail=none, style=dashed, color=black, arrowsize=1, penwidth=3, fontsize=10, ];
                wallet_updated -> profit_service [id="edge_7", arrowhead=normal, arrowtail=none, style=dotted, color=black, arrowsize=0.5, penwidth=0.8, fontsize=10];
                }""";
        response.tooltips = new ArrayList<>();
        response.tooltips.add(edge("edge_1", "POST /user<br>GET /user/:id"));
        return response;
    }

    private DiagramResponse.Tooltip edge(String id, String html) {
        var edge = new DiagramResponse.Tooltip();
        edge.id = id;
        edge.html = html;
        return edge;
    }
}
