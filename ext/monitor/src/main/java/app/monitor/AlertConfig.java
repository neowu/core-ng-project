package app.monitor;

import core.framework.api.json.Property;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;
import core.framework.log.Severity;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class AlertConfig {
    @NotNull
    @Property(name = "ignoreWarnings")
    public List<Matcher> ignoreWarnings = List.of();

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
    @Property(name = "channels")
    public Map<String, Matcher> channels = Map.of();

    @NotNull
    @Property(name = "kibanaURL")
    public String kibanaURL;

    public static class Matcher {
        @NotNull
        @Property(name = "apps")
        public List<String> apps = List.of();

        @Property(name = "severity")
        public Severity severity;

        @Property(name = "kibanaIndex")
        public String kibanaIndex;

        @NotNull
        @Property(name = "errorCodes")
        public List<String> errorCodes = List.of();
    }
}


