package core.framework.search;

import core.framework.search.impl.TestDocument;
import core.framework.search.module.InitSearchConfig;
import core.framework.search.module.SearchConfig;
import core.framework.test.module.AbstractTestModule;

import java.time.Duration;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        SearchConfig search = config(SearchConfig.class);
        search.host("localhost");
        search.slowOperationThreshold(Duration.ofSeconds(5));
        search.timeout(Duration.ofSeconds(10));
        search.type(TestDocument.class);

        config(InitSearchConfig.class).createIndex("document", "search-test/document-index.json");
    }
}
