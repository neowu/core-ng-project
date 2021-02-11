package core.visualization.web;

import java.util.ArrayList;
import java.util.List;

/**
 * @author allison
 */
public class ActionFlowAJAXServiceImpl implements ActionFlowAJAXService {
    @Override
    public ActionFlowResponse actionFlow(String actionId) {
        var response = new ActionFlowResponse();
        response.graph = """
            digraph G {
            "n0" [id="n0", label="website\\nAccountAJAXService", shape="box"];
            "n1" [id="n1", label="customer-service\\nRegisterService", shape="box", color=blue];
            "n2" [id="n2", label="wallet-service\\nWalletService", shape="box"];
            "n3" [id="n3", label="customer-service\\nLoginService", shape="box", color=red];
            "n4" [id="n4", label="customer-service\\nLoginHistoryService", shape="box"];
            "n0" -> "n1" [id="aaa", arrowhead="open", arrowtail="none", fontsize=10, label="action:/customer/register"];
            "n1" -> "n2" [id="bbb",arrowhead="open", arrowtail="none", fontsize=10, label="action:/wallet/create"];
            "n1" -> "n3" [id="ccc",arrowhead="open", arrowtail="none", fontsize=10, label="action:/customer/login"];
            "n3" -> "n4" [id="ddd",arrowhead="open", arrowtail="none", style=dashed, fontsize=10, label="action:/login-history"];
            }""";

        response.nodes = hardCodeNodes();
        response.edges = hardcodeEdges();
        return response;
    }

    private List<ActionFlowResponse.Node> hardCodeNodes() {
        List<ActionFlowResponse.Node> nodes = new ArrayList<>();
        ActionFlowResponse.Node node0 = new ActionFlowResponse.Node();
        node0.id = "n0";
        node0.cpuTime = 50;
        node0.dbElapsed = 10;
        nodes.add(node0);

        ActionFlowResponse.Node node1 = new ActionFlowResponse.Node();
        node1.id = "n1";
        node1.cpuTime = 60;
        node1.cacheHits = 1;
        nodes.add(node1);

        ActionFlowResponse.Node node2 = new ActionFlowResponse.Node();
        node2.id = "n2";
        node2.cpuTime = 30;
        node2.esElapsed = 100;
        nodes.add(node2);

        ActionFlowResponse.Node node3 = new ActionFlowResponse.Node();
        node3.id = "n3";
        node3.cpuTime = 80;
        node3.httpElapsed = 20;
        node3.kafkaElapsed = 10;
        nodes.add(node3);

        ActionFlowResponse.Node node5 = new ActionFlowResponse.Node();
        node5.id = "n4";
        node5.cpuTime = 30;
        node5.redisElapsed = 100;
        nodes.add(node5);

        return nodes;
    }

    private List<ActionFlowResponse.Edge> hardcodeEdges() {
        List<ActionFlowResponse.Edge> edges = new ArrayList<>();
        var edge1 = new ActionFlowResponse.Edge();
        edge1.id = "aaa";
        edge1.elapsed = 200;
        edge1.errorCode = "NOT_FOUND";
        edge1.errorMessage = "sth not found";
        edges.add(edge1);

        var edge2 = new ActionFlowResponse.Edge();
        edge2.id = "ccc";
        edge2.elapsed = 100;
        edge2.errorCode = "BAD_REQUEST";
        edge2.errorMessage = "invalid input";
        edges.add(edge2);

        return edges;
    }
}
