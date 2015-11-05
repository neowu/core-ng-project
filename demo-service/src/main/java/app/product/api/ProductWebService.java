package app.product.api;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.GET;
import core.framework.api.web.service.POST;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.ResponseStatus;

/**
 * @author neo
 */
public interface ProductWebService {
    @GET
    @Path("/product/:id(\\d+)")
    ProductView get(@PathParam("id") Integer id);

    @POST
    @Path("/product")
    @ResponseStatus(HTTPStatus.CREATED)
    void create(CreateProductRequest request);
}
