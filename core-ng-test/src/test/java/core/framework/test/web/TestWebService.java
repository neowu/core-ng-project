package core.framework.test.web;

import core.framework.api.web.service.PUT;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;

/**
 * @author neo
 */
public interface TestWebService {
    @PUT
    @Path("/test/:id")
    void put(@PathParam("id") Integer id);
}
