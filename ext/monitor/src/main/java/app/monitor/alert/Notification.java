package app.monitor.alert;

/**
 * @author neo
 */
public class Notification {
    public final String channel;
    public final Matcher matcher;

    public Notification(String channel, Matcher matcher) {
        this.channel = channel;
        this.matcher = matcher;
    }
}
