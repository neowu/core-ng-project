package app.product.web;

import app.product.domain.Product;
import app.product.service.ProductService;
import app.web.interceptor.Protected;
import core.framework.api.db.Database;
import core.framework.api.db.Transaction;
import core.framework.api.log.ActionLogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public class ProductController implements ProductWebService {
    private final Logger logger = LoggerFactory.getLogger(ProductController.class);
    @Inject
    ProductService productService;
    @Inject
    Database database;

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
//        if (id == 404) throw new NotFoundException("product not found, id=" + id);
//        if (id == 500) throw new Error("get product error, id=" + id);
        Product product = productService.get(id);
        return view(product);
    }

    @Override
    public void create(ProductView product) {
        product.id = 1;
        product.date = LocalDateTime.now();
        product.instant = Instant.now();
        productService.save(product);
    }

    @Override
    public List<ProductView> sync(List<ProductView> products) {
        try (Transaction transaction = database.beginTransaction()) {
            Optional<Integer> result = database.selectInt("select sleep(40)");// test long query timeout
            logger.debug("result={}", result);
            transaction.commit();
        }
        return products;
    }
}
