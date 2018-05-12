package core.framework.search;

import core.framework.search.config.InitSearchConfig;
import core.framework.search.config.SearchConfig;
import core.framework.search.test.TestDocument;
import core.framework.test.module.AbstractTestModule;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        SearchConfig searchConfig = config(SearchConfig.class);
        searchConfig.host("localhost");
        searchConfig.type(TestDocument.class);

        config(InitSearchConfig.class).createIndex("document", "search-test/document-index.json");
    }
}
