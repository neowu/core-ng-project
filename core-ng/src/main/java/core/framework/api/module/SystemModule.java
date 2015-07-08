package core.framework.api.module;

import core.framework.api.Module;

import java.nio.file.Paths;

/**
 * @author neo
 */
public class SystemModule extends Module {
    private final String propertyFileName;

    public SystemModule(String propertyFileName) {
        this.propertyFileName = propertyFileName;
    }

    @Override
    protected void initialize() {
        loadProperties(propertyFileName);

        property("sys.cache.host").ifPresent(host -> {
            if ("local".equals(host)) {
                cache().local();
            } else {
                cache().redis(host);
            }
        });

        property("sys.session.host").ifPresent(host -> {
            if ("local".equals(host)) {
                site().session().local();
            } else {
                site().session().redis(host);
            }
        });

        property("sys.log.traceLogPath").ifPresent(path -> log().traceLogPath(Paths.get(path)));
        property("sys.log.actionLogPath").ifPresent(path -> log().actionLogPath(Paths.get(path)));

        property("sys.rabbitMQ.host").ifPresent(hosts -> queue().rabbitMQ().hosts(hosts.split(",")));
        property("sys.rabbitMQ.user").ifPresent(user -> queue().rabbitMQ().user(user));
        property("sys.rabbitMQ.password").ifPresent(password -> queue().rabbitMQ().password(password));

        property("sys.jdbc.url").ifPresent(url -> db().url(url));
        property("sys.jdbc.user").ifPresent(user -> db().user(user));
        property("sys.jdbc.password").ifPresent(password -> db().password(password));
        property("sys.jdbc.pool.minSize").ifPresent(minSize -> {
            String maxSize = property("sys.jdbc.pool.maxSize").orElse(minSize);
            db().poolSize(Integer.parseInt(minSize), Integer.parseInt(maxSize));
        });
    }
}
