package app.product.web;

import app.product.api.CreateProductRequest;
import app.product.api.ProductView;
import app.product.api.ProductWebService;
import app.product.service.ProductService;
import core.framework.api.log.ActionLogContext;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author neo
 */
public class ProductWebServiceImpl implements ProductWebService {
    @Inject
    ProductService productService;

    @Override
    public Optional<ProductView> get(String id) {
        ActionLogContext.put("pid", id);
        return productService.get(id);
    }

    @Override
    public void create(CreateProductRequest request) {
        productService.create(request);
    }
}
