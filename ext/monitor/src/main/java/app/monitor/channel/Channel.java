package app.monitor.channel;

import app.monitor.alert.Alert;

import java.time.LocalDateTime;

/**
 * @author neo
 */
public interface Channel {
    void notify(String channel, Alert alert, int alertCountSinceLastSent, LocalDateTime now);
}
