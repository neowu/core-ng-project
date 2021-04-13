package core.diagram.web;

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
        String hours = request.queryParams().get("hours");
        String dot = diagramService.arch(hours == null ? 24 : Integer.parseInt(hours));
        var model = new DiagramModel();
        model.title = "System Architecture";
        model.dot = dot;
        return Response.html("/template/diagram.html", model);
    }
}
