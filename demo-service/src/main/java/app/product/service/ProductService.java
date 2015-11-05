package app.product.service;

import app.product.api.CreateProductRequest;
import app.product.api.ProductView;
import core.framework.api.util.Maps;
import core.framework.api.web.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author neo
 */
public class ProductService {
    private final Map<Integer, ProductView> products = Maps.newConcurrentHashMap();

    public ProductView get(int id) {
        ProductView view = products.get(id);
        if (view == null) throw new NotFoundException("product not found, id=" + id);
        return view;

    }

    public void create(CreateProductRequest request) {
        ProductView product = new ProductView();
        product.id = products.size() + 1;
        product.name = request.name;
        product.description = request.description;
        product.createdTime = LocalDateTime.now();
        products.put(product.id, product);
    }
}
