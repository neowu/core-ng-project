package core.log.web;

import core.framework.api.json.Property;
import core.framework.api.validate.NotBlank;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class SendEventRequest {
    @NotNull
    @Size(min = 1)
    @Property(name = "events")
    public List<Event> events = new ArrayList<>();

    public enum Result {
        @Property(name = "OK")
        OK,
        @Property(name = "WARN")
        WARN,
        @Property(name = "ERROR")
        ERROR
    }

    public static class Event {
        @NotNull
        @Property(name = "date")
        public ZonedDateTime date;
        @NotNull
        @Property(name = "result")
        public Result result;
        @NotBlank
        @Size(max = 200)
        @Property(name = "action")
        public String action;
        @NotBlank
        @Size(max = 200)
        @Property(name = "errorCode")
        public String errorCode;
        @Size(max = 1000)
        @Property(name = "errorMessage")
        public String errorMessage;
        @NotNull
        @Property(name = "context")
        public Map<String, String> context = new HashMap<>();
        @NotNull
        @Property(name = "stats")
        public Map<String, Double> stats = new HashMap<>();
        @NotNull
        @Property(name = "info")
        public Map<String, String> info = new HashMap<>();
        @NotNull
        @Property(name = "elapsedTime")
        public Long elapsedTime;
    }
}
