package app.service;

import app.domain.Product;
import core.framework.api.cache.Cache;
import core.framework.api.db.Database;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDateTime;

/**
 * @author neo
 */
public class ProductService {
    @Inject
    Database database;
    @Inject
    Cache<Product> cache;

    @Inject
    @Named("product-name-id")
    Cache<Integer> nameIdCache;

    public Product get(int id) {
        return cache.get(String.valueOf(id), () -> getProduct(id));
    }

    public Product getByName(String name) {
        Integer productId = nameIdCache.get(name, () -> 1);
        return get(productId);
    }

    private Product getProduct(int id) {
        Product product = new Product();
        product.id = id;
        product.name = "test";
        product.date = LocalDateTime.now();
        product.price = 10.99;
        product.description = "description";
        return product;
    }
}
