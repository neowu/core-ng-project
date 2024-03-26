package app.monitor.alert;

import app.monitor.AlertConfig;
import app.monitor.channel.ChannelManager;
import core.framework.inject.Inject;
import core.framework.internal.util.LRUMap;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author ericchung, neo
 */
public class AlertService {
    private final Map<String, AlertStat> stats = new LRUMap<>(1000);
    private final ReentrantLock lock = new ReentrantLock();
    private final String kibanaURL;
    private final Matchers ignoredErrors;
    private final Matchers criticalErrors;
    private final int timespanInMinutes;
    private final String site;
    private final List<Notification> notifications;
    @Inject
    ChannelManager channelManager;

    public AlertService(AlertConfig config) {
        site = config.site;
        kibanaURL = config.kibanaURL;
        ignoredErrors = new Matchers(config.ignoreErrors);
        criticalErrors = new Matchers(config.criticalErrors);
        notifications = config.notifications.stream()
            .map(notification -> new Notification(notification.channel, new Matcher(notification.matcher)))
            .collect(Collectors.toList());
        timespanInMinutes = config.timespanInHours * 60;
    }

    public void process(Alert alert) {
        Result result = check(alert);
        if (result.notify) {
            alert.kibanaURL = kibanaURL;
            alert.site = site;
            notify(alert, result);
        }
    }

    private void notify(Alert alert, Result result) {
        for (Notification notification : notifications) {
            if (notification.matcher.match(alert)) {
                channelManager.notify(notification.channel, alert, result.alertCountSinceLastSent);
            }
        }
    }

    Result check(Alert alert) {
        if (ignoredErrors.match(alert))
            return new Result(false, -1);

        String key = alertKey(alert);
        lock.lock();
        try {
            AlertStat stat = stats.get(key);
            if (stat == null) {
                stats.put(key, new AlertStat(alert.date));
                return new Result(true, -1);
            }
            int timespanInMinutes = criticalErrors.match(alert) ? 1 : this.timespanInMinutes;
            if (Duration.between(stat.lastSentDate, alert.date).toMinutes() >= timespanInMinutes) {
                stats.put(key, new AlertStat(alert.date));
                return new Result(true, stat.alertCountSinceLastSent);
            } else {
                stat.alertCountSinceLastSent++;
                return new Result(false, -1);
            }
        } finally {
            lock.unlock();
        }
    }

    String alertKey(Alert alert) {
        return alert.app + "/" + alert.action + "/" + alert.severity + "/" + alert.errorCode;    // WARN and ERROR may have same error code
    }

    static class AlertStat {
        final LocalDateTime lastSentDate;
        int alertCountSinceLastSent;

        AlertStat(LocalDateTime lastSentDate) {
            this.lastSentDate = lastSentDate;
        }
    }

    static class Result {
        final boolean notify;
        final int alertCountSinceLastSent;

        Result(boolean notify, int alertCountSinceLastSent) {
            this.notify = notify;
            this.alertCountSinceLastSent = alertCountSinceLastSent;
        }
    }
}
