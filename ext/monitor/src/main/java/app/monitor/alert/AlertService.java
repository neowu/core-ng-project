package app.monitor.alert;

import app.monitor.AlertConfig;
import app.monitor.slack.SlackClient;
import core.framework.inject.Inject;
import core.framework.internal.util.LRUMap;
import core.framework.log.Severity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.stream.Collectors;

import static core.framework.util.Strings.format;

/**
 * @author ericchung, neo
 */
public class AlertService {
    private final LRUMap<String, AlertStat> stats = new LRUMap<>(1000);
    private final String kibanaURL;
    private final AlertMatcher ignoredErrors;
    private final AlertMatcher criticalErrors;
    private final int timespanInMinutes;
    private final String[][] colors = {
            {"#ff5c33", "#ff9933"}, // 2 colors for warn, change color for weekly review of every week
            {"#a30101", "#e62a00"}  // 2 colors for error
    };
    private final String site;
    private final List<NotificationChannel> channels;
    @Inject
    SlackClient slackClient;

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
        LocalDateTime now = LocalDateTime.now();
        Result result = check(alert, now);
        if (result.notify) {
            notify(alert, result.alertCountSinceLastSent, now);
        }
    }

    Result check(Alert alert, LocalDateTime now) {
        if (ignoredErrors.matches(alert))
            return new Result(false, -1);
        if (criticalErrors.matches(alert))
            return new Result(true, -1);

        String key = alertKey(alert);
        synchronized (stats) {
            AlertStat stat = stats.get(key);
            if (stat == null) {
                stats.put(key, new AlertStat(now));
                return new Result(true, -1);
            } else if (Duration.between(stat.lastSentDate, now).toMinutes() >= timespanInMinutes) {
                stats.put(key, new AlertStat(now));
                return new Result(true, stat.alertCountSinceLastSent);
            } else {
                stat.alertCountSinceLastSent++;
                return new Result(false, -1);
            }
        }
    }

    private void notify(Alert alert, int alertCountSinceLastSent, LocalDateTime now) {
        String message = message(alert, alertCountSinceLastSent);
        String color = color(alert.severity, now);
        for (NotificationChannel channel : channels) {
            if (channel.matcher.matches(alert)) {
                slackClient.send(channel.channel, message, color);
            }
        }
    }

    String docURL(String kibanaIndex, String id) {
        return format("{}/app/kibana#/doc/{}-pattern/{}-*?id={}&_g=()", kibanaURL, kibanaIndex, kibanaIndex, id);
    }

    String message(Alert alert, int alertCountSinceLastSent) {
        String docURL = docURL(alert.kibanaIndex, alert.id);

        var builder = new StringBuilder(256);
        if (alertCountSinceLastSent > 0) builder.append("*[").append(alertCountSinceLastSent).append("]* ");

        builder.append(alert.severity).append(": *");
        if (site != null) builder.append(site).append(" / ");
        builder.append(alert.app).append("*\n");

        if (alert.host != null) builder.append("host: ").append(alert.host).append('\n');
        builder.append("_id: <").append(docURL).append('|').append(alert.id).append(">\n");
        if (alert.action != null) builder.append("action: ").append(alert.action).append('\n');

        builder.append("error_code: *").append(alert.errorCode).append("*\nmessage: ").append(alert.errorMessage).append('\n');

        return builder.toString();
    }

    String color(Severity severity, LocalDateTime now) {
        int week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int colorIndex = severity == Severity.WARN ? 0 : 1;
        return colors[colorIndex][(week - 1) % 2];
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
