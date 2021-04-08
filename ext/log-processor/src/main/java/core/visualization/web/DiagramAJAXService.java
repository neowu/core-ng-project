package core.visualization.web;

import core.framework.api.web.service.GET;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;

/**
 * @author allison
 */
public interface DiagramAJAXService {
    @GET
    @Path("/v2/ajax/action-flow/:actionId")
    DiagramResponse actionFlowV2(@PathParam("actionId") String actionId);

    @GET
    @Path("/ajax/action-flow/:actionId")
    DiagramResponse actionFlow(@PathParam("actionId") String actionId);

    @GET
    @Path("/ajax/action-flow")
    DiagramResponse overall();
}
