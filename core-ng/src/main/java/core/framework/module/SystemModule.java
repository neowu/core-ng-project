package core.framework.module;

import core.framework.util.Properties;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class SystemModule extends Module {
    private static final String SYS_JDBC_URL = "sys.jdbc.url";
    private static final String SYS_JDBC_USER = "sys.jdbc.user";
    private static final String SYS_JDBC_PASSWORD = "sys.jdbc.password";
    private static final String SYS_LOG_APPENDER = "sys.log.appender";
    private static final String SYS_HTTP_ALLOW_CIDR = "sys.http.allowCIDR";
    private static final String SYS_HTTP_PORT = "sys.http.port";
    private static final String SYS_HTTPS_PORT = "sys.https.port";
    private static final String SYS_SECURITY_CSP = "sys.security.csp";
    private static final String SYS_PUBLISH_API_ALLOW_CIDR = "sys.publishAPI.allowCIDR";
    private static final String SYS_CDN_HOST = "sys.cdn.host";
    private static final String SYS_SESSION_HOST = "sys.session.host";
    private static final String SYS_CACHE_HOST = "sys.cache.host";
    private static final String SYS_REDIS_HOST = "sys.redis.host";
    private static final String SYS_KAFKA_URI = "sys.kafka.uri";

    private final Logger logger = LoggerFactory.getLogger(SystemModule.class);
    private final Set<String> allowedKeys = Set.of(SYS_JDBC_URL, SYS_JDBC_USER, SYS_JDBC_PASSWORD,
            SYS_LOG_APPENDER, SYS_HTTP_ALLOW_CIDR, SYS_HTTP_PORT, SYS_HTTPS_PORT,
            SYS_SECURITY_CSP, SYS_PUBLISH_API_ALLOW_CIDR, SYS_CDN_HOST, SYS_SESSION_HOST,
            SYS_CACHE_HOST, SYS_REDIS_HOST, SYS_KAFKA_URI, "sys.elasticsearch.host", "sys.mongo.uri"); // allow elasticsearch and mongo as optional modules
    private final String propertyFileClasspath;

    public SystemModule(String propertyFileClasspath) {
        this.propertyFileClasspath = propertyFileClasspath;
    }

    @Override
    protected void initialize() {
        logger.info("load system module properties, classpath={}", propertyFileClasspath);
        var properties = new Properties();
        properties.load(propertyFileClasspath);
        loadProperties(properties);

        configureHTTP();
        configureCache();
        configureLog();
        property(SYS_KAFKA_URI).ifPresent(uri -> kafka().uri(uri));
        configureDB();
        property(SYS_REDIS_HOST).ifPresent(host -> redis().host(host));
        configureSite();
    }

    void loadProperties(Properties properties) {
        for (String key : properties.keys()) {
            if (!allowedKeys.contains(key)) throw new Error(format("found unknown system module key, key={}, allowedKeys={}", key, allowedKeys));
            context.propertyManager.properties.set(key, properties.get(key).orElse(null));
        }
    }

    private void configureCache() {
        property(SYS_CACHE_HOST).ifPresent(host -> {
            if ("local".equals(host)) {
                cache().local();
            } else {
                cache().redis(host);
            }
        });
    }

    void configureSite() {
        property(SYS_SESSION_HOST).ifPresent(host -> {
            if ("local".equals(host)) {
                site().session().local();
            } else {
                site().session().redis(host);
            }
        });
        property(SYS_CDN_HOST).ifPresent(host -> site().cdn().host(host));
        property(SYS_PUBLISH_API_ALLOW_CIDR).ifPresent(cidrs -> site().publishAPI(Strings.split(cidrs, ',')));
        property(SYS_SECURITY_CSP).ifPresent(policy -> site().security().contentSecurityPolicy(policy));
    }

    void configureHTTP() {
        property(SYS_HTTP_PORT).ifPresent(port -> http().httpPort(Integer.parseInt(port)));
        property(SYS_HTTPS_PORT).ifPresent(port -> http().httpsPort(Integer.parseInt(port)));
        property(SYS_HTTP_ALLOW_CIDR).ifPresent(cidrs -> http().allowCIDR(Strings.split(cidrs, ',')));
    }

    private void configureLog() {
        property(SYS_LOG_APPENDER).ifPresent(appender -> {
            if ("console".equals(appender)) {
                log().appendToConsole();
            } else {
                log().appendToKafka(appender);
            }
        });
    }

    private void configureDB() {
        property(SYS_JDBC_URL).ifPresent(url -> db().url(url));
        property(SYS_JDBC_USER).ifPresent(user -> db().user(user));
        property(SYS_JDBC_PASSWORD).ifPresent(password -> db().password(password));
    }
}
