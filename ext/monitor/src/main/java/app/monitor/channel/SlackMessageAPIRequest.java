package app.monitor.channel;

import core.framework.api.json.Property;

import java.util.List;

/**
 * @author ericchung
 */
public class SlackMessageAPIRequest {
    @Property(name = "channel")
    public String channel;

    @Property(name = "attachments")
    public List<Attachment> attachments;

    public static class Attachment {
        @Property(name = "color")
        public String color;

        @Property(name = "text")
        public String text;
    }
}
