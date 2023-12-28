package app.monitor.channel;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

/**
 * @author ajax
 */
public class PagerDutyAccidentAPIRequest {
    @NotNull
    @Property(name = "incident")
    public Incident incident;

    public static class Incident {
        @NotNull
        @Property(name = "type")
        public String type;

        @NotNull
        @Property(name = "title")
        public String title;

        @NotNull
        @Property(name = "service")
        public Service service;

        @Property(name = "priority")
        public Priority priority;

        @Property(name = "urgency")
        public String urgency;

        @Property(name = "body")
        public Body body;

        @Property(name = "escalation_policy")
        public EscalationPolicy escalationPolicy;
    }

    public static class Service {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "type")
        public String type;
    }

    public static class Priority {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "type")
        public String type;
    }

    public static class Body {
        @NotNull
        @Property(name = "type")
        public String type;

        @NotNull
        @Property(name = "details")
        public String details;
    }

    public static class EscalationPolicy {
        @NotNull
        @Property(name = "id")
        public String id;

        @NotNull
        @Property(name = "type")
        public String type;
    }
}
