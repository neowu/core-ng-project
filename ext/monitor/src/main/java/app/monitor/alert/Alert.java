package app.monitor.alert;

import core.framework.log.Severity;

import java.time.LocalDateTime;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class Alert {
    public String id;
    public LocalDateTime date;
    public String app;
    public String action;
    public Severity severity;
    public String errorCode;
    public String errorMessage;
    public String kibanaIndex;
    public String host;

    // provided by alert service from global config
    public String kibanaURL;
    public String site;

    public void severity(String result) {
        severity = "WARN".equals(result) ? Severity.WARN : Severity.ERROR;
    }

    public String docURL() {
        return format("{}/app/kibana#/doc/{}-pattern/{}-*?id={}&_g=()", kibanaURL, kibanaIndex, kibanaIndex, id);
    }
}
