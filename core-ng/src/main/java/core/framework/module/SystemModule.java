package core.framework.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author neo
 */
public final class SystemModule extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemModule.class);

    private final String propertyFileClasspath;

    public SystemModule(String propertyFileClasspath) {
        this.propertyFileClasspath = propertyFileClasspath;
    }

    @Override
    protected void initialize() {
        loadProperties(propertyFileClasspath);

        configureHTTP();
        configureCache();
        configureLog();
        property("sys.kafka.uri").ifPresent(uri -> kafka().uri(uri));
        configureDB();
        property("sys.redis.host").ifPresent(host -> redis().host(host));
        configureSite();
    }

    private void configureCache() {
        property("sys.cache.host").ifPresent(host -> {
            if ("local".equals(host)) {
                cache().local();
            } else {
                cache().redis(host);
            }
        });
    }

    void configureSite() {
        property("sys.session.host").ifPresent(host -> {
            if ("local".equals(host)) {
                site().session().local();
            } else {
                site().session().redis(host);
            }
        });
        property("sys.cdn.host").ifPresent(host -> site().cdn().host(host));
        property("sys.security.csp").ifPresent(policy -> site().security().contentSecurityPolicy(policy));
        Optional.ofNullable(property("sys.api.allowCIDR")
                .orElseGet(() -> {
                    var cidrs = property("sys.publishAPI.allowCIDR");
                    if (cidrs.isPresent()) {
                        LOGGER.warn("'sys.publishAPI.allowCIDR' has been deprecated and will be removed in version 1.2. "
                            + "please use 'sys.api.allowCIDR' as soon as possible.");
                        return cidrs.get();
                    }
                    return null;
                }))
            .ifPresent(cidrs -> site().allowAPI(new IPv4RangePropertyValueParser(cidrs).parse()));
    }

    void configureHTTP() {
        property("sys.http.port").ifPresent(port -> http().httpPort(Integer.parseInt(port)));
        property("sys.https.port").ifPresent(port -> http().httpsPort(Integer.parseInt(port)));
        property("sys.http.allowCIDR").ifPresent(cidrs -> http().access().allow(new IPv4RangePropertyValueParser(cidrs).parse()));
    }

    private void configureLog() {
        property("sys.log.appender").ifPresent(appender -> {
            if ("console".equals(appender)) {
                log().appendToConsole();
            } else {
                log().appendToKafka(appender);
            }
        });
    }

    private void configureDB() {
        property("sys.jdbc.url").ifPresent(url -> db().url(url));
        property("sys.jdbc.user").ifPresent(user -> db().user(user));
        property("sys.jdbc.password").ifPresent(password -> db().password(password));
    }
}
