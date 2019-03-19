package core.log.web;

import core.framework.api.json.Property;
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
        @Property(name = "id")
        public String id;
        @NotNull
        @Property(name = "date")
        public ZonedDateTime date;
        @NotNull
        @Property(name = "type")
        public String type;
        @NotNull
        @Property(name = "result")
        public Result result;
        @NotNull
        @Property(name = "context")
        public Map<String, String> context = new HashMap<>();
        @Property(name = "errorMessage")
        public String errorMessage;
        @Property(name = "exceptionStackTrace")
        public String exceptionStackTrace;
        @NotNull
        @Property(name = "elaspedTime")
        public Long elaspedTime;
    }
}
