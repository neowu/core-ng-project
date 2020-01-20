package app.monitor.slack;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.util.List;

/**
 * @author ericchung
 */
public class SlackMessageAPIRequest {
    @NotNull
    @Property(name = "channel")
    public String channel;

    @Property(name = "attachments")
    public List<Attachment> attachments;

    public static class Attachment {
        @NotNull
        @Property(name = "color")
        public String color;

        @NotNull
        @Property(name = "text")
        public String text;
    }
}
