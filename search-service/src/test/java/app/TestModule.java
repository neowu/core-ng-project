package app;

import app.domain.ProductIndex;
import app.domain.SKUIndex;
import app.service.SearchProductService;
import core.framework.api.AbstractTestModule;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchBuilder;
import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Files;
import core.framework.api.util.YAML;

import java.nio.file.Path;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        Path dataPath = Files.tempDirectory();
        ElasticSearch search = new ElasticSearchBuilder().local(dataPath).get();
        overrideBinding(ElasticSearch.class, null, search);

        search.createIndex("main", ClasspathResources.text("mappings.json"));
        onShutdown(() -> Files.deleteDirectory(dataPath));

        load(new SearchServiceApp());

        indexTestData(search);
    }

    private void indexTestData(ElasticSearch search) {
        SearchProductService searchService = bean(SearchProductService.class);
        YAML.loadList(ProductIndex.class, ClasspathResources.text("products.yml")).forEach(searchService::index);
        YAML.loadList(SKUIndex.class, ClasspathResources.text("skus.yml")).forEach(searchService::index);
        search.flush("main");
    }
}
