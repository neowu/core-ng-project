package app.monitor.alert;

import app.monitor.AlertConfig;
import app.monitor.channel.ChannelManager;
import core.framework.inject.Inject;
import core.framework.internal.util.LRUMap;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ericchung, neo
 */
public class AlertService {
    private final LRUMap<String, AlertStat> stats = new LRUMap<>(1000);
    private final String kibanaURL;
    private final AlertMatcher ignoredErrors;
    private final AlertMatcher criticalErrors;
    private final int timespanInMinutes;
    private final String site;
    private final List<NotificationChannel> channels;
    @Inject
    ChannelManager channelManager;

    public AlertService(AlertConfig config) {
        site = config.site;
        kibanaURL = config.kibanaURL;
        ignoredErrors = new AlertMatcher(config.ignoreErrors);
        criticalErrors = new AlertMatcher(config.criticalErrors);
        channels = config.channels.entrySet().stream()
                .map(entry -> new NotificationChannel(entry.getKey(), new AlertMatcher(List.of(entry.getValue()))))
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
        for (NotificationChannel channel : channels) {
            if (channel.matcher.matches(alert)) {
                channelManager.notify(channel.channel, alert, result.alertCountSinceLastSent);
            }
        }
    }

    Result check(Alert alert) {
        if (ignoredErrors.matches(alert))
            return new Result(false, -1);
        if (criticalErrors.matches(alert))
            return new Result(true, -1);

        String key = alertKey(alert);
        synchronized (stats) {
            AlertStat stat = stats.get(key);
            if (stat == null) {
                stats.put(key, new AlertStat(alert.date));
                return new Result(true, -1);
            } else if (Duration.between(stat.lastSentDate, alert.date).toMinutes() >= timespanInMinutes) {
                stats.put(key, new AlertStat(alert.date));
                return new Result(true, stat.alertCountSinceLastSent);
            } else {
                stat.alertCountSinceLastSent++;
                return new Result(false, -1);
            }
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
