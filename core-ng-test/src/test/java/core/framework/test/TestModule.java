package core.framework.test;

import core.framework.db.IsolationLevel;
import core.framework.http.HTTPClient;
import core.framework.kafka.Message;
import core.framework.scheduler.Job;
import core.framework.test.db.TestDBEntity;
import core.framework.test.inject.TestBean;
import core.framework.test.kafka.TestMessage;
import core.framework.test.module.AbstractTestModule;
import core.framework.test.scheduler.TestJob;
import core.framework.test.web.TestWebService;
import core.framework.test.web.TestWebServiceClientInterceptor;
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

        load(new OverrideBeanTest());

        overrideBinding(HTTPClient.class, Mockito.mock(HTTPClient.class));  // in test context, override binding is defined before actual binding
        bind(HTTPClient.class, HTTPClient.builder().maxRetries(3).retryWaitTime(Duration.ofSeconds(1)).enableCookie().build());

        configureDB();
        configureKafka();
        configureRedis();

        log().appendToKafka("localhost");

        configureCache();

        configureHTTP();
        configureSite();
        configureAPI();

        bind(new TestBean(requiredProperty("test.inject-test.property")));

        configureJob();
        configureExecutor();

        highCPUUsageThreshold(0.8);
        highHeapUsageThreshold(0.8);

        onShutdown(() -> {
        });
    }

    private void configureAPI() {
        api().client(TestWebService.class, "https://localhost:8443").intercept(new TestWebServiceClientInterceptor());
        api().publishAPI(List.of("0.0.0.0/0"));
    }

    private void configureRedis() {
        redis().host("localhost");

        redis("redis2").host("localhost");
    }

    private void configureCache() {
        cache().redis("localhost");
        cache().maxLocalSize(5000);
        cache().remote(TestDBEntity.class, Duration.ofHours(6));
        cache().local(TestMessage.class, Duration.ofHours(6));
    }

    private void configureExecutor() {
        executor().add();
        executor().add("name", 1);
    }

    private void configureSite() {
        site().session().redis("localhost");
        site().session().timeout(Duration.ofMinutes(30));
        site().session().cookie("SessionId", "localhost");
        site().cdn().host("//cdn");
        site().security().contentSecurityPolicy("default-src 'self' https://cdn; img-src 'self' https://cdn data:; object-src 'none'; frame-src 'none';");
    }

    private void configureHTTP() {
        http().httpPort(8080);
        http().httpsPort(8443);
        http().gzip();
        http().maxForwardedIPs(2);
        http().access().allow(List.of("0.0.0.0/0"));
        http().access().deny(List.of("10.0.0.0/24"));
    }

    private void configureKafka() {
        kafka().uri("kafka://localhost:9092");
        kafka().maxProcessTime(Duration.ofMinutes(30));
        kafka().longConsumerLagThreshold(Duration.ofSeconds(60));
        kafka().poolSize(1);
        kafka().groupId("test");
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
        db().maxOperations(5000);
        db().repository(TestDBEntity.class);
        initDB().createSchema();
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
