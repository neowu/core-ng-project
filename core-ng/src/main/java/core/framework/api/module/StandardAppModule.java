package core.framework.api.module;

import core.framework.api.Module;

import java.nio.file.Paths;

/**
 * @author neo
 */
public class StandardAppModule extends Module {
    private final String propertyFileName;

    public StandardAppModule(String propertyFileName) {
        this.propertyFileName = propertyFileName;
    }

    @Override
    protected void initialize() {
        loadProperties(propertyFileName);

        property("app.cache.host").ifPresent(host -> {
            if ("local".equals(host)) {
                cache().local();
            } else {
                cache().redis(host);
            }
        });

        property("app.session.host").ifPresent(host -> {
            if ("local".equals(host)) {
                site().session().local();
            } else {
                site().session().redis(host);
            }
        });

        property("app.log.traceLogPath").ifPresent(path -> log().traceLogPath(Paths.get(path)));
        property("app.log.actionLogPath").ifPresent(path -> log().actionLogPath(Paths.get(path)));

        property("app.rabbitMQ.host").ifPresent(hosts -> queue().rabbitMQ().hosts(hosts));
        property("app.rabbitMQ.user").ifPresent(user -> queue().rabbitMQ().user(user));
        property("app.rabbitMQ.password").ifPresent(password -> queue().rabbitMQ().password(password));
    }
}
