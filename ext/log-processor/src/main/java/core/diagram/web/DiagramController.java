package core.diagram.web;

import core.diagram.service.DiagramService;
import core.framework.inject.Inject;
import core.framework.util.Strings;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class DiagramController {
    @Inject
    DiagramService diagramService;

    public Response arch(Request request) {
        int hours = hours(request.queryParams());
        Set<String> excludes = excludes(request.queryParams());
        String dot = diagramService.arch(hours, excludes);
        var model = new DiagramModel();
        model.title = "System Architecture";
        model.dot = dot;
        return Response.html("/template/diagram.html", model);
    }

    private int hours(Map<String, String> params) {
        String hours = params.get("hours");
        return hours == null ? 24 : Integer.parseInt(hours);
    }

    private Set<String> excludes(Map<String, String> params) {
        String excludes = params.get("excludes");
        return excludes == null ? Set.of() : Set.of(Strings.split(excludes, ','));
    }
}
