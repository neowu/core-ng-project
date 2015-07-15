package app;

import app.domain.Product;
import app.service.ProductService;
import core.framework.api.AbstractApplication;
import core.framework.api.module.SystemModule;

import java.time.Duration;

/**
 * @author neo
 */
public class DemoApp extends AbstractApplication {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));

        cache().add(Product.class, Duration.ofSeconds(60));
        cache().add("product-name-id", Integer.class, Duration.ofSeconds(60));

        db().repository(Product.class);
        bind(ProductService.class);

//        Mongo mongo = bind(new MongoBuilder()
//            .uri("mongodb://192.168.2.2:27017")
//            .databaseName("main")
//            .entityClass(User.class)
//            .viewClass(MongoUserAggregateView.class)
//            .get());
//        onShutdown(mongo::shutdown);

        // web part
        load(new WebModule());

//        load(new JobModule());

//        load(new MongoTest());

//        load(new QueueModule());
    }
}
