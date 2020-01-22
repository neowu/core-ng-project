package app.monitor.alert;

import app.monitor.AlertConfig;
import app.monitor.slack.SlackClient;
import core.framework.inject.Inject;
import core.framework.internal.util.LRUMap;
import core.framework.log.Severity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author ericchung, neo
 */
public class AlertService {
    private final LRUMap<String, AlertStat> stats = new LRUMap<>(1000);
    private final String kibanaURL;
    private final ErrorCodeMatchers ignoredWarnings;
    private final ErrorCodeMatchers criticalErrors;
    private final int timespanInMinutes;
    private final String[][] colors = {
            {"#ff5c33", "#ff9933"}, // 2 colors for warn, change color for weekly review of every week
            {"#a30101", "#e62a00"}  // 2 colors for error
    };
    private final String site;
    private final Map<String, String> channels;
    @Inject
    SlackClient slackClient;

    public AlertService(AlertConfig config) {
        site = config.site;
        kibanaURL = config.kibanaURL;
        ignoredWarnings = new ErrorCodeMatchers(config.ignoreWarnings);
        criticalErrors = new ErrorCodeMatchers(config.criticalErrors);
        channels = Map.of("trace/WARN", config.channel.actionWarn,
                "trace/ERROR", config.channel.actionError,
                "event/WARN", config.channel.eventWarn,
                "event/ERROR", config.channel.eventError);
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
        if (alert.severity == Severity.WARN && ignoredWarnings.matches(alert.app, alert.errorCode))
            return new Result(false, -1);
        if (alert.severity == Severity.ERROR && criticalErrors.matches(alert.app, alert.errorCode))
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
        slackClient.send(alertChannel(alert), message, color);
    }

    String docURL(String kibanaIndex, String id) {
        return format("{}/app/kibana#/doc/{}-pattern/{}-*?id={}&_g=()", kibanaURL, kibanaIndex, kibanaIndex, id);
    }

    String message(Alert alert, int alertCountSinceLastSent) {
        String count = alertCountSinceLastSent > 0 ? "*[" + alertCountSinceLastSent + "]*" : "";
        String app = site == null ? alert.app : site + " / " + alert.app;
        String docURL = docURL(alert.kibanaIndex, alert.id);
        return format("{}{}: *{}*\nid: <{}|{}>\nerrorCode: *{}*\nmessage: {}\n",
                count, alert.severity, app,
                docURL, alert.id,
                alert.errorCode, alert.errorMessage);
    }

    String color(Severity severity, LocalDateTime now) {
        int week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int colorIndex = severity == Severity.WARN ? 0 : 1;
        return colors[colorIndex][(week - 1) % 2];
    }

    String alertChannel(Alert alert) {
        String channel = channels.get(alert.kibanaIndex + "/" + alert.severity);
        if (channel == null) throw new Error(format("channel is not defined, kibanaIndex={}, severity={}", alert.kibanaIndex, alert.severity));
        return channel;
    }

    String alertKey(Alert alert) {
        return alert.app + "/" + alert.severity + "/" + alert.errorCode;    // WARN and ERROR may have same error code
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
