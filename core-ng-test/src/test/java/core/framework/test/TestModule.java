package core.framework.test;

import core.framework.api.AbstractTestModule;
import core.framework.test.mongo.TestEntity;
import core.framework.test.search.TestDocument;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        mongo().uri("mongodb://localhost/test");
        mongo().collection(TestEntity.class);

        search().type(TestDocument.class);

        initSearch().createIndex("document", "search/document-index.json");
    }
}
