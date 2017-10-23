package core.framework.test;

import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.test.db.TestDBEntity;
import core.framework.test.db.TestSequenceIdDBEntity;
import core.framework.test.kafka.TestMessage;
import core.framework.test.module.AbstractTestModule;
import core.framework.test.mongo.TestMongoEntity;
import core.framework.test.search.TestDocument;
import org.mockito.Mockito;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        overrideBinding(HTTPClient.class, Mockito.mock(HTTPClient.class));  // in test context, override binding is defined before actual binding
        bind(new HTTPClientBuilder().build());

        db().url("jdbc:mysql://localhost:3306/test");
        db().repository(TestDBEntity.class);
        initDB().createSchema();

        db("oracle").url("jdbc:oracle:thin:@localhost:1521/test");
        db("oracle").repository(TestSequenceIdDBEntity.class);
        initDB("oracle").createSchema();

        mongo().uri("mongodb://localhost:27017/test");
        mongo().collection(TestMongoEntity.class);

        search().host("localhost");
        search().type(TestDocument.class);
        initSearch().createIndex("document", "search/document-index.json");

        kafka().uri("localhost:9092");
        kafka().publish("topic", TestMessage.class);
    }
}
