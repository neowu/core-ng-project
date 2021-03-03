package core.visualization.web;

import core.framework.api.web.service.GET;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;

/**
 * @author allison
 */
public interface ActionFlowAJAXService {
    @GET
    @Path("/v1/ajax/action-flow/:actionId")
    ActionFlowResponseV1 actionFlowV1(@PathParam("actionId") String actionId);

    @GET
    @Path("/v2/ajax/action-flow/:actionId")
    ActionFlowResponseV2 actionFlowV2(@PathParam("actionId") String actionId);

    @GET
    @Path("/ajax/action-flow/:actionId")
    ActionFlowResponse actionFlow(@PathParam("actionId") String actionId);
}
