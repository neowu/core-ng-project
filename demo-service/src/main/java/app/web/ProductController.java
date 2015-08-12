package app.web;

import app.domain.Product;
import app.service.ProductService;
import app.web.interceptor.Protected;
import core.framework.api.log.ActionLogContext;
import core.framework.api.web.exception.NotFoundException;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author neo
 */
public class ProductController implements ProductWebService {
    @Inject
    ProductService productService;

    private ProductView view(Product product) {
        ProductView view = new ProductView();
        view.id = product.id;
        view.name = product.name;
        view.date = product.date;
        view.price = product.price;
        view.description = product.description;
        return view;
    }

    @Override
    @Protected(operation = "get-product")
    public ProductView get(Integer id) {
        ActionLogContext.put("pid", id);
        if (id == 404) throw new NotFoundException("product not found, id=" + id);
        if (id == 500) throw new Error("get product error, id=" + id);
        Product product = productService.get(id);
        return view(product);
    }

    @Override
    public void create(ProductView product) {
        product.id = 1;
        product.date = LocalDateTime.now();
        product.instant = Instant.now();
    }

    @Override
    public List<ProductView> sync(List<ProductView> products) {
        return products;
    }
}
