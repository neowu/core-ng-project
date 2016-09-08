package core.framework.test;

import core.framework.api.AbstractTestModule;
import core.framework.test.db.TestDBEntity;
import core.framework.test.mongo.TestMongoEntity;
import core.framework.test.search.TestDocument;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        db().repository(TestDBEntity.class);
        initDB().createSchema();

        mongo().collection(TestMongoEntity.class);

        search().type(TestDocument.class);
        initSearch().createIndex("document", "search/document-index.json");
    }
}
