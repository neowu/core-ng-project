package core.log.web;

import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.asm.CodeBuilder;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.internal.web.management.ServiceResponse;
import core.framework.json.JSON;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

/**
 * @author neo
 */
public class ServiceGraphController implements Controller {
    private final IPv4AccessControl accessControl = new IPv4AccessControl();

    @Override
    public Response execute(Request request) {
        accessControl.validate(request.clientIP());

        ServiceGraphRequest graphRequest = request.bean(ServiceGraphRequest.class);
        HTTPClient client = new HTTPClientBuilder().build();  // create ad hoc http client will be recycled once done
        var builder = new CodeBuilder();
        builder.append("digraph {\n");
        for (String serviceURL : graphRequest.serviceURLs) {
            HTTPResponse response = client.execute(new HTTPRequest(HTTPMethod.GET, serviceURL + "/_sys/service"));
            ServiceResponse serviceResponse = JSON.fromJSON(ServiceResponse.class, response.text());
            buildKafkaGraph(builder, graphRequest, serviceResponse);
            buildAppNodeGraph(builder, graphRequest, serviceResponse);
        }

        return Response.text(builder.append("}\n").build());
    }

    private void buildKafkaGraph(CodeBuilder builder, ServiceGraphRequest request, ServiceResponse response) {
        builder.append("subgraph cluster_kafka {\n");
        builder.indent(1).append("label=kafka\n");
        for (String messageClass : response.consumers) {
            builder.indent(1).append("\"{}\" [{}];\n", messageClass, style(request.messageNodeStyle));
        }
        for (String messageClass : response.producers) {
            builder.indent(1).append("\"{}\" [{}];\n", messageClass, style(request.messageNodeStyle));
        }
        builder.append("}\n");
    }

    private void buildAppNodeGraph(CodeBuilder builder, ServiceGraphRequest request, ServiceResponse response) {
        builder.append("subgraph cluster_{} {\n", response.app.replace('-', '_'));
        builder.indent(1).append("label=\"{}\";\n", response.app);
        builder.indent(1).append("\"{}\" [{}];\n", response.app, style(request.appNodeStyle));

        for (String serviceClass : response.services) {
            builder.indent(1).append("\"{}\" [{}];\n", serviceClass, style(request.serviceNodeStyle));
            builder.indent(1).append("\"{}\" -> \"{}\" [{}];\n", serviceClass, response.app, style(request.serviceEdgeStyle));
        }
        for (String clientClass : response.clients) {
            builder.append("\"{}\" -> \"{}\" [{}];\n", response.app, clientClass, style(request.serviceEdgeStyle));
        }
        for (String messageClass : response.producers) {
            builder.indent(1).append("\"{}\" -> \"{}\" [{}];\n", response.app, messageClass, style(request.messageEdgeStyle));
        }
        for (String messageClass : response.consumers) {
            builder.indent(1).append("\"{}\" -> \"{}\" [{}];\n", messageClass, response.app, style(request.messageEdgeStyle));
        }
        builder.append("}\n");
    }

    private String style(String style) {
        return style == null ? "" : style;
    }
}
