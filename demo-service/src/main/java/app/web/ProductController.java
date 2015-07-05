package app.web;

import app.domain.Product;
import app.service.ProductService;
import core.framework.api.log.ActionLogContext;

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
    public ProductView get(Integer id) {
        ActionLogContext.put("pid", id);
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
