package core.log;

import core.framework.api.AbstractTestModule;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchBuilder;
import core.framework.api.util.ClasspathResources;
import core.framework.api.util.Files;

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

        search.createIndex("action", ClasspathResources.text("action-index.json"));
        search.createIndex("trace", ClasspathResources.text("trace-index.json"));
        onShutdown(() -> Files.deleteDirectory(dataPath));

        load(new LogProcessorApp());
    }
}
