package core;

import core.framework.http.HTTPMethod;
import core.framework.module.Module;
import core.framework.web.site.WebDirectory;
import core.visualization.web.VisualizationController;

/**
 * @author neo
 */
public class VisualizationModule extends Module {
    @Override
    protected void initialize() {
        var controller = new VisualizationController(bean(WebDirectory.class));
        http().route(HTTPMethod.GET, "/vis", controller::home);
    }
}
