package core.framework.test;

import core.framework.api.AbstractTestModule;
import core.framework.test.db.TestDBEntity;
import core.framework.test.db.TestSequenceIdDBEntity;
import core.framework.test.kafka.TestMessage;
import core.framework.test.mongo.TestMongoEntity;
import core.framework.test.search.TestDocument;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        db().url("jdbc:mysql://localhost:3306/test");
        db().repository(TestDBEntity.class);
        initDB().createSchema();

        db("seq").url("jdbc:oracle:thin:@localhost:1521/test");
        db("seq").repository(TestSequenceIdDBEntity.class);
        initDB("seq").createSchema();

        mongo().uri("mongodb://localhost:27017/test");
        mongo().collection(TestMongoEntity.class);

        search().host("localhost");
        search().type(TestDocument.class);
        initSearch().createIndex("document", "search/document-index.json");

        kafka().uri("localhost:9092");
        kafka().publish("topic", TestMessage.class);
    }
}
