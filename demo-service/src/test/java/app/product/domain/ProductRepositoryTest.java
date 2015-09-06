package app.product.domain;

import app.IntegrationTest;
import core.framework.api.db.Repository;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author neo
 */
public class ProductRepositoryTest extends IntegrationTest {
    @Inject
    Repository<Product> productRepository;

    @Test
    public void insert() {
        Product product = new Product();
        product.name = "test";
        product.description = "desc";
        product.date = LocalDateTime.now();
        Optional<Long> id = productRepository.insert(product);
        Assert.assertTrue(id.isPresent());
    }

    @Test
    public void update() {
        Product product = new Product();
        product.name = "test";
        product.description = "desc";
        product.date = LocalDateTime.now();
        product.id = productRepository.insert(product).get().intValue();

        product.name = "test1";
        productRepository.update(product);

        Product selectedProduct = productRepository.get(product.id).get();

        Assert.assertEquals(product.name, selectedProduct.name);
    }
}
