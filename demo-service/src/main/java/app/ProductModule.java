package app;

import app.product.domain.Product;
import app.product.domain.ProductDocument;
import app.product.service.ProductService;
import app.product.service.SearchProductService;
import app.product.web.ProductController;
import app.product.web.ProductWebService;
import core.framework.api.Module;
import core.framework.api.db.IsolationLevel;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchBuilder;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.Types;

import java.time.Duration;

/**
 * @author neo
 */
public class ProductModule extends Module {
    @Override
    protected void initialize() {
        cache().add(Product.class, Duration.ofSeconds(60));
        cache().add("product-name-id", Integer.class, Duration.ofSeconds(60));

        db().defaultIsolationLevel(IsolationLevel.READ_UNCOMMITTED);

        db().repository(Product.class);

        bind(ProductService.class);

        configureSearch();

//        queue().publish("rabbitmq://queue/test", CreateProductRequest.class);
//
//        queue().subscribe("rabbitmq://queue/test")
//            .handle(CreateProductRequest.class, bind(CreateProductRequestHandler.class))
//            .maxConcurrentHandlers(100);

        api().service(ProductWebService.class, bind(ProductController.class));
        api().client(ProductWebService.class, "http://localhost:8080");
    }

    private void configureSearch() {
        ElasticSearch search = bindSupplier(ElasticSearch.class, null, new ElasticSearchBuilder()
            .remote("192.168.2.2"));
        onShutdown(search::close);

        bind(Types.generic(ElasticSearchType.class, ProductDocument.class), null, search.type("main", "product", ProductDocument.class));
        bind(SearchProductService.class);
    }
}
