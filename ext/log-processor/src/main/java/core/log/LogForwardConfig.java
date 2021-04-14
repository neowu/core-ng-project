package core.log;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;

import java.util.List;

/**
 * @author neo
 */
public class LogForwardConfig {
    @NotNull
    @Property(name = "kafkaURI")
    public String kafkaURI;

    @Property(name = "action")
    public Forward action;

    @Property(name = "event")
    public Forward event;

    public static class Forward {
        @NotNull
        @Property(name = "topic")
        public String topic;

        @NotNull
        @Size(min = 1)
        @Property(name = "apps")
        public List<String> apps = List.of();   // apps must not be empty, as if we forwards all apps, the app consumes forwarded log will generates action-log back, to create a infinite loop

        @NotNull
        @Property(name = "ignoreErrorCodes")
        public List<String> ignoreErrorCodes = List.of();
    }
}
