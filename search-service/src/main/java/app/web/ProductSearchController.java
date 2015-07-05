package app.web;

import app.domain.ProductIndex;
import app.domain.SKUIndex;
import app.service.SearchProductRequest;
import app.service.SearchProductService;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

/**
 * @author neo
 */
public class ProductSearchController {
    @Inject
    SearchProductService searchService;

    // this is not RESTFul or proper layered, just for demo/test of ES
    public Response search(Request request) {
        String query = request.queryParam("q").get();
        SearchProductRequest searchProductRequest = new SearchProductRequest();
        searchProductRequest.query = query;
        List<ProductIndex> result = searchService.search(searchProductRequest);
        return Response.bean(result);
    }

    public Response index(Request request) {
        for (int i = 0; i < 10; i++) {
            ProductIndex product = new ProductIndex();
            product.id = i;
            product.name = "some product " + i;
            searchService.index(product);

            for (int j = 0; j < 10; j++) {
                SKUIndex sku = new SKUIndex();
                sku.productId = product.id;
                sku.sku = UUID.randomUUID().toString();
                sku.name = "sku " + j;
                searchService.index(sku);
            }
        }
        return Response.text("ok", "text/plain");
    }
}
