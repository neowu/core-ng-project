package app.web;

import core.framework.api.util.Lists;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author neo
 */
public class APITestController {
    @Inject
    ProductWebService productWebServiceClient;

    public Response get(Request request) {
        ProductView view = new ProductView();
        view.id = 1;
        view.name = "test";
        view.date = LocalDateTime.now();
        view.price = 10.0;
        productWebServiceClient.create(view);

        List<ProductView> view1 = productWebServiceClient.sync(Lists.newArrayList(view));
        return Response.bean(view1);
    }
}
