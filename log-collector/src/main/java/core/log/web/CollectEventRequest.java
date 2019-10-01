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
public class CollectEventRequest {
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
        @Property(name = "action")
        public String action;
        @NotBlank
        @Property(name = "errorCode")
        public String errorCode;
        @Property(name = "errorMessage")
        public String errorMessage;
        @NotNull
        @Property(name = "context")
        public Map<String, String> context = new HashMap<>();
        @NotNull
        @Property(name = "info")
        public Map<String, String> info = new HashMap<>();
        @NotNull
        @Property(name = "elapsedTime")
        public Long elapsedTime;
    }
}
