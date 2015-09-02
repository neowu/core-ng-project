package app;

import app.product.domain.ProductDocument;
import app.product.service.SearchProductService;
import app.product.web.ProductController;
import app.user.domain.MongoUserAggregateView;
import app.user.domain.User;
import core.framework.api.AbstractTestModule;
import core.framework.api.mongo.MockMongoBuilder;
import core.framework.api.mongo.Mongo;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchBuilder;
import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Files;
import core.framework.api.util.YAML;
import org.mockito.Mockito;

import java.nio.file.Path;

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

        Path dataPath = Files.tempDirectory();
        ElasticSearch search = new ElasticSearchBuilder().local(dataPath).get();
        overrideBinding(ElasticSearch.class, null, search);

        search.createIndex("main", ClasspathResources.text("mappings.json"));
        onShutdown(() -> Files.deleteDirectory(dataPath));

        load(new DemoServiceApp());

        initDB().createSchema();
//        initDB().script("db.sql");
        indexTestData(search);
    }

    private void indexTestData(ElasticSearch search) {
        SearchProductService searchService = bean(SearchProductService.class);
        YAML.loadList(ProductDocument.class, ClasspathResources.text("products.yml")).forEach(searchService::index);
        search.flush("main");
    }
}
