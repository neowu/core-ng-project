package app.monitor.channel;

import app.monitor.alert.Alert;

import java.util.Map;

/**
 * @author neo
 */
public interface Channel {
    void notify(String channel, Map<String, String> params, Alert alert, int alertCountSinceLastSent);
}
