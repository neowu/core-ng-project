package app.monitor.alert;

import app.monitor.AlertConfig;

import java.util.List;

/**
 * @author neo
 */
public class Matchers {
    public final List<Matcher> matchers;

    public Matchers(List<AlertConfig.Matcher> matchers) {
        this.matchers = matchers.stream().map(Matcher::new).toList();
    }

    boolean match(Alert alert) {
        for (Matcher matcher : matchers) {
            if (matcher.match(alert)) {
                return true;
            }
        }
        return false;
    }
}
