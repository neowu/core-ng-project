package app.monitor.alert;

/**
 * @author neo
 */
public class NotificationChannel {
    public final String channel;
    public final AlertMatcher matcher;

    public NotificationChannel(String channel, AlertMatcher matcher) {
        this.channel = channel;
        this.matcher = matcher;
    }
}
