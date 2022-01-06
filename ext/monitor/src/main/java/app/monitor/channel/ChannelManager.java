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

    // channelURI is type://id?key=value&key=value, or id with default channel
    public void notify(String channelURI, Alert alert, int alertCountSinceLastSent) {
        ChannelURI uri = parseChannelURI(channelURI);
        Channel channel = channels.get(uri.type == null ? defaultChannelType : uri.type);
        if (channel == null) throw new Error("channel not found, channelURI=" + channelURI);
        channel.notify(uri.id, uri.params, alert, alertCountSinceLastSent);
    }

    ChannelURI parseChannelURI(String channelURI) {
        ChannelURI uri = new ChannelURI();
        final int ptcDelimiterLen = 3;
        final int ptcIndex = channelURI.indexOf("://");
        if (ptcIndex < 0) {
            uri.id = channelURI;
            return uri;
        }
        final int paramsIndex = channelURI.indexOf('?');
        if (paramsIndex < 0) {
            uri.type = channelURI.substring(0, ptcIndex);
            uri.id = channelURI.substring(ptcIndex + ptcDelimiterLen);
            return uri;
        }
        uri.type = channelURI.substring(0, ptcIndex);
        uri.id = channelURI.substring(ptcIndex + ptcDelimiterLen, paramsIndex);
        String parameters = channelURI.substring(paramsIndex + 1);
        uri.params = Arrays.stream(Strings.split(parameters, '&'))
            .map(entry -> Strings.split(entry, '='))
            .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        return uri;
    }
}
