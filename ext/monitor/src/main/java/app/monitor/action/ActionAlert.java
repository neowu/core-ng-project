package app.monitor.action;

import core.framework.log.Severity;

/**
 * @author neo
 */
public class ActionAlert {
    public String id;
    public String app;
    public Severity severity;
    public String errorCode;
    public String errorMessage;
    public String kibanaIndex;
}
