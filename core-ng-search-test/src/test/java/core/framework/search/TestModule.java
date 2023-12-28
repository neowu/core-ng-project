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
        search.timeout(Duration.ofSeconds(5));
        search.maxResultWindow(1000);
        search.type(TestDocument.class);

        InitSearchConfig initSearch = config(InitSearchConfig.class);
        initSearch.putIndex("document", "search-test/document-index.json");
        initSearch.putIndexTemplate("document", "search-test/document-index-template.json");
        initSearch.flush("document");

        // test multiple search in one app
        search = config(SearchConfig.class, "other");
        search.host("localhost");
        search.type(TestDocument.class);
    }
}
