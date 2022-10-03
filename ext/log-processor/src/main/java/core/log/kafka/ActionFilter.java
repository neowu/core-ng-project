package core.log.kafka;

import core.framework.log.message.ActionLogMessage;
import core.log.LogFilterConfig;

import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
public class ActionFilter {
    private final List<Matcher> ignoreTrace;

    public ActionFilter(LogFilterConfig.ActionFilter filter) {
        this.ignoreTrace = filter.ignoreTrace.stream().map(Matcher::new).toList();
    }

    public boolean ignoreTrace(ActionLogMessage message) {
        for (Matcher matcher : ignoreTrace) {
            if (matcher.match(message)) return true;
        }
        return false;
    }

    public static class Matcher {
        final Set<String> apps;
        final Set<String> errorCodes;

        public Matcher(LogFilterConfig.Matcher matcher) {
            this.apps = Set.copyOf(matcher.apps);
            this.errorCodes = Set.copyOf(matcher.errorCodes);
        }

        boolean match(ActionLogMessage message) {
            return (apps.isEmpty() || message.app != null && apps.contains(message.app))
                   && (errorCodes.isEmpty() || message.errorCode != null && errorCodes.contains(message.errorCode));
        }
    }
}
