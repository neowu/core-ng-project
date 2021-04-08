package core;

import core.framework.http.HTTPMethod;
import core.framework.module.Module;
import core.visualization.service.ActionFlowService;
import core.visualization.web.DiagramAJAXService;
import core.visualization.web.DiagramAJAXServiceImplV2;
import core.visualization.web.DiagramController;

/**
 * @author neo
 */
public class DiagramModule extends Module {
    @Override
    protected void initialize() {
        bind(ActionFlowService.class);
        api().service(DiagramAJAXService.class, bind(DiagramAJAXServiceImplV2.class));
        http().route(HTTPMethod.GET, "/vis", bind(DiagramController.class)::home);
    }
}
