package core.framework.module;

import core.framework.util.Strings;

/**
 * @author neo
 */
public final class SystemModule extends Module {
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
        property("sys.publishAPI.allowCIDR").ifPresent(cidrs -> site().publishAPI(Strings.split(cidrs, ',')));
        property("sys.webSecurity.trustedSources").ifPresent(sources -> site().webSecurity(Strings.split(sources, ',')));
    }

    void configureHTTP() {
        property("sys.http.port").ifPresent(port -> http().httpPort(Integer.parseInt(port)));
        property("sys.https.port").ifPresent(port -> http().httpsPort(Integer.parseInt(port)));
        property("sys.http.allowCIDR").ifPresent(cidrs -> http().allowCIDR(Strings.split(cidrs, ',')));
    }

    private void configureLog() {
        property("sys.log.appender").ifPresent(appender -> {
            if ("console".equals(appender)) {
                log().writeToConsole();
            } else {
                log().writeToKafka(appender);
            }
        });
    }

    private void configureDB() {
        property("sys.jdbc.url").ifPresent(url -> db().url(url));
        property("sys.jdbc.user").ifPresent(user -> db().user(user));
        property("sys.jdbc.password").ifPresent(password -> db().password(password));
    }
}
