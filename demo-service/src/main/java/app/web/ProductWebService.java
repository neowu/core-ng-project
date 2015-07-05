package app.web;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.service.GET;
import core.framework.api.web.service.POST;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.ResponseStatus;

import java.util.List;

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
    void create(ProductView product);

    // this is not restful, just for testing input/output as list
    @POST
    @Path("/product/sync")
    List<ProductView> sync(List<ProductView> products);
}
