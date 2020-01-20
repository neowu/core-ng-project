package app.monitor.slack;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

/**
 * @author ericchung
 */
public class SlackMessageAPIResponse {
    @NotNull
    @Property(name = "ok")
    public Boolean ok;
}
