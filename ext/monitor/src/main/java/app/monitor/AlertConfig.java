package app.monitor;

import core.framework.api.json.Property;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;
import core.framework.log.Severity;

import java.util.List;

/**
 * @author neo
 */
public class AlertConfig {
    @NotNull
    @Property(name = "ignoreErrors")
    public List<Matcher> ignoreErrors = List.of();

    @NotNull
    @Property(name = "criticalErrors")
    public List<Matcher> criticalErrors = List.of();

    @Min(0)
    @NotNull
    @Property(name = "timespanInHours")
    public Integer timespanInHours = 4;

    @Property(name = "site")
    public String site;

    @NotNull
    @Size(min = 1)
    @Property(name = "notifications")
    public List<Notification> notifications = List.of();

    @NotNull
    @Property(name = "kibanaURL")
    public String kibanaURL;

    public static class Matcher {
        @NotNull
        @Property(name = "apps")
        public List<String> apps = List.of();

        @Property(name = "severity")
        public Severity severity;

        @NotNull
        @Property(name = "indices")
        public List<String> indices = List.of();

        @NotNull
        @Property(name = "errorCodes")
        public List<String> errorCodes = List.of();
    }

    public static class Notification {
        @NotNull
        @Property(name = "channel")
        public String channel;

        @NotNull
        @Property(name = "matcher")
        public Matcher matcher;
    }
}
