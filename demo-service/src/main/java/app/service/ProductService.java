package app.service;

import app.domain.Product;
import core.framework.api.cache.Cache;
import core.framework.api.db.Database;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
