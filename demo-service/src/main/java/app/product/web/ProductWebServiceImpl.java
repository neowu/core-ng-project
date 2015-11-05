package app.product.web;

import app.product.api.CreateProductRequest;
import app.product.api.ProductView;
import app.product.api.ProductWebService;
import app.product.service.ProductService;
import core.framework.api.log.ActionLogContext;

import javax.inject.Inject;

/**
 * @author neo
 */
public class ProductWebServiceImpl implements ProductWebService {
    @Inject
    ProductService productService;

    @Override
    public ProductView get(Integer id) {
        ActionLogContext.put("pid", id);
        return productService.get(id);
    }

    @Override
    public void create(CreateProductRequest request) {
        productService.create(request);
    }
}
