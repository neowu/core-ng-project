package core.diagram.web;

import core.diagram.service.Diagram;
import core.diagram.service.DiagramService;
import core.framework.inject.Inject;
import core.framework.web.Request;
import core.framework.web.Response;

/**
 * @author neo
 */
public class DiagramController {
    @Inject
    DiagramService diagramService;

    public Response arch(Request request) {
        Diagram diagram = diagramService.arch();
        var model = new DiagramModel();
        model.title = "System Architecture";
        model.diagram = diagram;
        return Response.html("/template/diagram.html", model);
    }
}
