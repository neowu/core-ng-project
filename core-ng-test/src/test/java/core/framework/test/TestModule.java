package core.framework.test;

import core.framework.db.IsolationLevel;
import core.framework.http.HTTPClient;
import core.framework.kafka.Message;
import core.framework.scheduler.Job;
import core.framework.test.db.TestDBEntity;
import core.framework.test.db.TestDBEntityWithJSON;
import core.framework.test.db.TestDBProjection;
import core.framework.test.db.TestDBView;
import core.framework.test.inject.TestBean;
import core.framework.test.kafka.TestMessage;
import core.framework.test.module.AbstractTestModule;
import core.framework.test.scheduler.TestJob;
import core.framework.test.web.TestWebService;
import core.framework.test.web.TestWebServiceClientInterceptor;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        loadProperties("test.properties");

        load(new OverrideBeanTest());

        overrideBinding(HTTPClient.class, mock(HTTPClient.class));  // in test context, override binding is defined before actual binding
        bind(HTTPClient.class, HTTPClient.builder().maxRetries(3).retryWaitTime(Duration.ofSeconds(1)).enableCookie().enableFallbackDNSCache().build());

        configureDB();
        configureKafka();
        configureRedis();

        log().appendToKafka("localhost");

        configureCache();

        configureHTTP();
        configureSite();
        api().client(TestWebService.class, "https://localhost:8443").intercept(new TestWebServiceClientInterceptor());

        bind(new TestBean(requiredProperty("test.inject-test.property")));

        configureJob();

        highCPUUsageThreshold(0.8);
        highHeapUsageThreshold(0.8);
        highMemUsageThreshold(0.8);

        onShutdown(() -> {
        });
    }

    private void configureRedis() {
        redis().host("localhost");
        redis().password("password");

        redis("redis2").host("localhost");
    }

    private void configureCache() {
        cache().redis("localhost", "password");
        cache().maxLocalSize(5000);
        cache().add(TestDBEntity.class, Duration.ofHours(6));
    }

    private void configureSite() {
        site().session().redis("localhost");
        site().session().timeout(Duration.ofMinutes(30));
        site().session().cookie("SessionId", "localhost");
        site().cdn().host("//cdn");
        site().security().contentSecurityPolicy("default-src 'self' https://cdn; img-src 'self' https://cdn data:; object-src 'none'; frame-src 'none';");
        site().allowAPI(List.of("0.0.0.0/0"));
        site().message(List.of("messages/messages.properties"));
    }

    private void configureHTTP() {
        http().listenHTTP("8080");
        http().listenHTTPS("0.0.0.0:8443");
        http().gzip();
        http().maxForwardedIPs(2);
        http().maxProcessTime(Duration.ofSeconds(30));
        http().maxEntitySize(10000000);
        http().access().allow(List.of("0.0.0.0/0"));
        http().access().deny(List.of("10.0.0.0/24"));
        http().errorHandler((request, e) -> Optional.empty());
    }

    private void configureKafka() {
        kafka().uri("localhost:9092");
        kafka().maxRequestSize(2 * 1024 * 1024);
        kafka().longConsumerDelayThreshold(Duration.ofSeconds(60));
        kafka().concurrency(1);
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
        db().poolSize(5, 5);
        db().longTransactionThreshold(Duration.ofSeconds(5));
        db().repository(TestDBEntity.class);
        db().repository(TestDBEntityWithJSON.class);
        db().view(TestDBView.class);
        db().view(TestDBProjection.class);
        initDB().createSchema();
    }

    private void configureJob() {
        schedule().timeZone(ZoneId.of("UTC"));
        Job job = new TestJob();
        schedule().fixedRate("fixed-rate-job", job, Duration.ofSeconds(10));
        schedule().hourlyAt("hourly-job", job, 30, 0);
        schedule().dailyAt("daily-job", job, LocalTime.NOON);
        schedule().weeklyAt("weekly-job", job, DayOfWeek.MONDAY, LocalTime.NOON);
        schedule().monthlyAt("monthly-job", job, 1, LocalTime.NOON);
        schedule().trigger("trigger-job", job, previous -> previous.plusHours(1));
    }
}
