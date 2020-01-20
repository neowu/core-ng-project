package app.monitor.action;

import app.monitor.ActionAlertConfig;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class IgnoredWarnings {
    public final List<Warnings> warnings;

    public IgnoredWarnings(ActionAlertConfig config) {
        warnings = config.ignoreWarnings.stream()
                                        .map(warning -> new Warnings(Set.copyOf(warning.apps), Set.copyOf(warning.errorCodes)))
                                        .collect(Collectors.toList());
    }

    boolean ignore(ActionAlert alert) {
        for (Warnings warning : warnings) {
            if (warning.apps.contains(alert.app)) {
                return warning.errorCodes.contains(alert.errorCode);
            }
        }
        return false;
    }

    static class Warnings {
        final Set<String> apps;
        final Set<String> errorCodes;

        Warnings(Set<String> apps, Set<String> errorCodes) {
            this.apps = apps;
            this.errorCodes = errorCodes;
        }
    }
}
