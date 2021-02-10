package core.visualization.web;

import core.framework.api.web.service.GET;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;

/**
 * @author allison
 */
public interface ActionFlowAJAXService {
    @GET
    @Path("/ajax/action-flow/:actionId")
    ActionFlowResponse actionFlow(@PathParam("actionId") String actionId);
}
