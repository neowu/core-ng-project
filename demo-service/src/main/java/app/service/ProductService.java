package app.service;

import app.domain.Product;
import app.web.ProductView;
import core.framework.api.cache.Cache;
import core.framework.api.db.Repository;
import core.framework.api.web.exception.NotFoundException;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author neo
 */
public class ProductService {
    @Inject
    Repository<Product> repository;
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
        return repository.get(id).orElseThrow(() -> new NotFoundException("product not found, id=" + id));
    }

    public void save(ProductView view) {
        Product product = new Product();
        product.name = view.name;
        product.description = view.description;
        product.date = view.date;
        product.price = view.price;
        repository.insert(product);
    }
}
