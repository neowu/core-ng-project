package core;

import core.diagram.service.DiagramService;
import core.diagram.web.DiagramController;
import core.diagram.web.DiagramModel;
import core.framework.http.HTTPMethod;
import core.framework.module.Module;

/**
 * @author neo
 */
public class DiagramModule extends Module {
    @Override
    protected void initialize() {
        bind(DiagramService.class);

        site().template("/template/diagram.html", DiagramModel.class);
        DiagramController controller = bind(DiagramController.class);
        http().route(HTTPMethod.GET, "/diagram/arch", controller::arch);
        http().route(HTTPMethod.GET, "/diagram/action", controller::action);
    }
}
