package app.monitor.channel;

import app.monitor.alert.Alert;

import java.util.Map;

/**
 * @author neo
 */
public class ChannelManager {
    private final Map<String, Channel> channels;
    private final String defaultChannelType;

    public ChannelManager(Map<String, Channel> channels, String defaultChannelType) {
        this.channels = channels;
        this.defaultChannelType = defaultChannelType;
    }

    // channelURI is type://id, or id with default channel
    public void notify(String channelURI, Alert alert, int alertCountSinceLastSent) {
        String[] values = parseChannelURI(channelURI);
        String type = values[0];
        String id = values[1];
        Channel channel = channels.get(type == null ? defaultChannelType : type);
        if (channel == null) throw new Error("channel not found, channelURI=" + channelURI);
        channel.notify(id, alert, alertCountSinceLastSent);
    }

    String[] parseChannelURI(String channelURI) {
        final int index = channelURI.indexOf("://");
        if (index < 0) return new String[]{null, channelURI};
        return new String[]{channelURI.substring(0, index), channelURI.substring(index + 3)};
    }
}
