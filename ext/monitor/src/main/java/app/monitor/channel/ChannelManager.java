package app.monitor.channel;

import app.monitor.alert.Alert;
import core.framework.util.Strings;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void notify(String channelURI, Alert alert, int alertCountSinceLastSent) {
        ChannelURI uri = parseChannelURI(channelURI);
        Channel channel = channels.get(uri.type == null ? defaultChannelType : uri.type);
        if (channel == null) throw new Error("channel not found, channelURI=" + channelURI);
        channel.notify(uri.id, uri.params, alert, alertCountSinceLastSent);
    }

    // channelURI is type://id?key=value&key=value, or id with default channel
    ChannelURI parseChannelURI(String channelURI) {
        var uri = new ChannelURI();
        final int protocolIndex = channelURI.indexOf("://");
        if (protocolIndex < 0) {
            uri.id = channelURI;
            return uri;
        }
        int paramsIndex = channelURI.indexOf('?');
        if (paramsIndex < 0) {
            uri.type = channelURI.substring(0, protocolIndex);
            uri.id = channelURI.substring(protocolIndex + 3);
            return uri;
        }
        uri.type = channelURI.substring(0, protocolIndex);
        uri.id = channelURI.substring(protocolIndex + 3, paramsIndex);
        String parameters = channelURI.substring(paramsIndex + 1);
        uri.params = Arrays.stream(Strings.split(parameters, '&'))
            .map(entry -> Strings.split(entry, '='))
            .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        return uri;
    }
}
