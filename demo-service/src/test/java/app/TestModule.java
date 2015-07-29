package app;

import app.domain.MongoUserAggregateView;
import app.domain.User;
import app.web.ProductController;
import core.framework.api.AbstractTestModule;
import core.framework.api.mongo.MockMongoBuilder;
import core.framework.api.mongo.Mongo;
import org.mockito.Mockito;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        overrideBinding(ProductController.class, Mockito.mock(ProductController.class));
        overrideBinding(Mongo.class, new MockMongoBuilder()
            .uri("mongodb://localhost/main")
            .entityClass(User.class)
            .viewClass(MongoUserAggregateView.class).get());

        load(new DemoApp());

        initDB().createSchema();
//        initDB().script("db.sql");
    }
}
