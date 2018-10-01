package core.framework.test;

import core.framework.db.IsolationLevel;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.kafka.Message;
import core.framework.scheduler.Job;
import core.framework.test.db.TestDBEntity;
import core.framework.test.db.TestSequenceIdDBEntity;
import core.framework.test.inject.TestBean;
import core.framework.test.kafka.TestMessage;
import core.framework.test.module.AbstractTestModule;
import core.framework.test.scheduler.TestJob;
import org.mockito.Mockito;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        loadProperties("test.properties");

        overrideBinding(HTTPClient.class, Mockito.mock(HTTPClient.class));  // in test context, override binding is defined before actual binding
        bind(HTTPClient.class, new HTTPClientBuilder().enableCookie().enableRedirect().build());

        configureDB();
        configureKafka();

        redis().host("localhost");

        log().writeToKafka("localhost:9092");

        configureCache();

        configureHTTP();
        configureSite();

        bind(new TestBean(requiredProperty("test.inject-test.property")));

        configureJob();

        configureExecutor();
    }

    private void configureCache() {
        cache().redis("localhost");
        cache().add(String.class, Duration.ofHours(6));
    }

    private void configureExecutor() {
        executor().add(null, 1);
        executor().add("name", 1);
    }

    private void configureSite() {
        site().session().redis("localhost");
        site().session().timeout(Duration.ofMinutes(30));
        site().session().cookie("SessionId", "localhost");
        site().cdn().host("//cdn");
        site().security().contentSecurityPolicy("default-src 'self' https://cdn; img-src 'self' https://cdn data:; object-src 'none'; frame-src 'none';");
        site().publishAPI("0.0.0.0/0");
    }

    private void configureHTTP() {
        http().httpPort(8080);
        http().httpsPort(8443);
        http().gzip();
        http().maxForwardedIPs(2);
        http().allowCIDR("0.0.0.0/0");
    }

    private void configureKafka() {
        kafka().uri("kafka://localhost:9092");
        kafka().maxProcessTime(Duration.ofMinutes(30));
        kafka().poolSize(1);
        kafka().publish("topic", TestMessage.class);
        kafka().subscribe("topic1", TestMessage.class, (List<Message<TestMessage>> messages) -> {
        });
        kafka().subscribe("topic2", TestMessage.class, (String key, TestMessage message) -> {
        });
    }

    private void configureDB() {
        db().url("jdbc:mysql://localhost:3306/test");
        db().isolationLevel(IsolationLevel.READ_UNCOMMITTED);
        db().timeout(Duration.ofSeconds(10));
        db().batchSize(7);
        db().slowOperationThreshold(Duration.ofSeconds(5));
        db().longTransactionThreshold(Duration.ofSeconds(5));
        db().tooManyRowsReturnedThreshold(1000);
        db().repository(TestDBEntity.class);
        initDB().createSchema();

        db("oracle").url("jdbc:oracle:thin:@localhost:1521/test");
        db().isolationLevel(IsolationLevel.READ_COMMITTED);
        db("oracle").repository(TestSequenceIdDBEntity.class);
        initDB("oracle").createSchema();
    }

    private void configureJob() {
        schedule().timeZone(ZoneId.of("UTC"));
        Job job = new TestJob();
        schedule().fixedRate("fixed-rate-job", job, Duration.ofSeconds(10));
        schedule().dailyAt("daily-job", job, LocalTime.NOON);
        schedule().weeklyAt("weekly-job", job, DayOfWeek.MONDAY, LocalTime.NOON);
        schedule().monthlyAt("monthly-job", job, 1, LocalTime.NOON);
        schedule().trigger("trigger-job", job, previous -> previous.plusHours(1));
    }
}
