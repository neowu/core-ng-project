package core.framework.test;

import core.framework.db.IsolationLevel;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.kafka.Message;
import core.framework.test.db.TestDBEntity;
import core.framework.test.db.TestSequenceIdDBEntity;
import core.framework.test.inject.TestBean;
import core.framework.test.kafka.TestMessage;
import core.framework.test.module.AbstractTestModule;
import core.framework.test.mongo.TestMongoEntity;
import core.framework.test.search.TestDocument;
import core.framework.util.Sets;
import org.mockito.Mockito;

import java.util.List;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        loadProperties("test.properties");

        overrideBinding(HTTPClient.class, Mockito.mock(HTTPClient.class));  // in test context, override binding is defined before actual binding
        bind(HTTPClient.class, new HTTPClientBuilder().build());

        configureDB();
        configureMongo();
        configureSearch();
        configureKafka();

        redis().host("localhost");

        log().writeToConsole();

        site().session().redis("localhost");
        configureHTTP();

        bind(new TestBean(requiredProperty("test.inject-test.property")));
    }

    private void configureHTTP() {
        http().httpPort(8080);
        http().httpsPort(8443);
        http().enableGZip();
        http().allowClientIP(Sets.newHashSet("0.0.0.0/0"));
        http().maxForwardedIPs(2);
    }

    private void configureKafka() {
        kafka().uri("kafka://localhost:9092");
        kafka().publish("topic", TestMessage.class);
        kafka().subscribe("topic1", TestMessage.class, (List<Message<TestMessage>> messages) -> {
        });
        kafka().subscribe("topic2", TestMessage.class, (String key, TestMessage message) -> {
        });
    }

    private void configureSearch() {
        search().host("localhost");
        search().type(TestDocument.class);
        initSearch().createIndex("document", "search-test/document-index.json");
    }

    private void configureDB() {
        db().url("jdbc:mysql://localhost:3306/test");
        db().defaultIsolationLevel(IsolationLevel.READ_UNCOMMITTED);
        db().repository(TestDBEntity.class);
        initDB().createSchema();

        db("oracle").url("jdbc:oracle:thin:@localhost:1521/test");
        db().defaultIsolationLevel(IsolationLevel.READ_COMMITTED);
        db("oracle").repository(TestSequenceIdDBEntity.class);
        initDB("oracle").createSchema();
    }

    private void configureMongo() {
        mongo().uri("mongodb://localhost:27017/test");
        mongo().collection(TestMongoEntity.class);
    }
}
