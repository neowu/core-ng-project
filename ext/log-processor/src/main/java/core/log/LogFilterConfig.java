package core.log;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.util.List;

/**
 * @author neo
 */
public class LogFilterConfig {  // there could be massive traces if site is under CC attack or scanning, this is to reduce unnecessary storage / log
    @Property(name = "action")
    public ActionFilter action;

    public static class ActionFilter {
        @NotNull
        @Property(name = "ignoreTrace")
        public List<Matcher> ignoreTrace = List.of();
    }

    public static class Matcher {
        @NotNull
        @Property(name = "apps")
        public List<String> apps = List.of();

        @NotNull
        @Property(name = "errorCodes")
        public List<String> errorCodes = List.of();
    }
}
