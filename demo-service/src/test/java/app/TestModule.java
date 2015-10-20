package app;

import app.product.domain.ProductDocument;
import app.product.web.ProductController;
import app.product.web.ProductView;
import app.product.web.ProductWebService;
import core.framework.api.AbstractTestModule;
import core.framework.api.util.ClasspathResources;
import core.framework.api.util.YAML;
import core.framework.impl.search.ElasticSearchTypeImpl;
import org.mockito.Mockito;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        overrideBinding(ProductController.class, Mockito.mock(ProductController.class));

        load(new DemoServiceApp());

        // specify mock web service client behavior
        Mockito.when(bean(ProductWebService.class).get(1)).thenReturn(new ProductView());

        initDB().createSchema();
        initDB().runScript("db.sql");

        initSearch().createIndex("main", "product-index.json");
        ElasticSearchTypeImpl<ProductDocument> productType = initSearch().type(ProductDocument.class);
        YAML.loadList(ProductDocument.class, ClasspathResources.text("products.yml")).forEach(product -> productType.index(String.valueOf(product.id), product));
        initSearch().flush("main");
    }
}
