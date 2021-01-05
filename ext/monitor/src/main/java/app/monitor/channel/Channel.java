package app.monitor.channel;

import app.monitor.alert.Alert;

/**
 * @author neo
 */
public interface Channel {
    void notify(String channel, Alert alert, int alertCountSinceLastSent);
}
