package core.diagram.web;

import core.diagram.service.DiagramService;
import core.framework.inject.Inject;
import core.framework.util.Strings;
import core.framework.web.Request;
import core.framework.web.Response;
import core.framework.web.exception.BadRequestException;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class DiagramController {
    @Inject
    DiagramService diagramService;

    public Response arch(Request request) {
        Map<String, String> params = request.queryParams();

        int hours = intParam(params, "hours", 24);
        List<String> includes = listParam(params, "includes");
        List<String> excludes = listParam(params, "excludes");

        String dot = diagramService.arch(hours, includes, excludes);
        var model = new DiagramModel();
        model.title = "System Architecture";
        model.dot = dot;
        return Response.html("/template/diagram.html", model);
    }

    public Response action(Request request) {
        String actionId = request.queryParams().get("actionId");
        if (actionId == null) throw new BadRequestException("actionId must not be null");
        String dot = diagramService.action(actionId);
        var model = new DiagramModel();
        model.title = "Action Flow";
        model.dot = dot;
        return Response.html("/template/diagram.html", model);
    }

    int intParam(Map<String, String> params, String name, int defaultvalue) {
        String hours = params.get(name);
        return hours == null ? defaultvalue : Integer.parseInt(hours);
    }

    List<String> listParam(Map<String, String> params, String paramName) {
        String value = params.get(paramName);
        return value == null ? List.of() : List.of(Strings.split(value, ','));
    }
}
