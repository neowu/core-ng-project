package app.monitor;

import core.framework.api.json.Property;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;

import java.util.List;

/**
 * @author neo
 */
public class AlertConfig {
    @NotNull
    @Property(name = "ignoreWarnings")
    public List<IgnoreWarnings> ignoreWarnings = List.of();

    @NotNull
    @Property(name = "criticalErrors")
    public List<String> criticalErrors = List.of();

    @Min(0)
    @NotNull
    @Property(name = "timespanInHours")
    public Integer timespanInHours = 4;

    @Property(name = "site")
    public String site;

    @NotNull
    @Property(name = "channel")
    public Channel channel;

    @NotNull
    @Property(name = "kibanaURL")
    public String kibanaURL;

    public static class IgnoreWarnings {
        @NotNull
        @Property(name = "apps")
        public List<String> apps = List.of();

        @NotNull
        @Property(name = "errorCodes")
        public List<String> errorCodes = List.of();
    }

    public static class Channel {
        @NotNull
        @Property(name = "eventError")
        public String eventError;
        @NotNull
        @Property(name = "eventWarn")
        public String eventWarn;
        @NotNull
        @Property(name = "actionError")
        public String actionError;
        @NotNull
        @Property(name = "actionWarn")
        public String actionWarn;
    }
}


