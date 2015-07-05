package app;

import app.web.ProductController;
import core.framework.api.AbstractTestModule;
import org.mockito.Mockito;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        overrideBinding(ProductController.class, Mockito.mock(ProductController.class));

        load(new DemoApp());

        initDB().createSchema();
//        initDB().script("db.sql");
    }
}
