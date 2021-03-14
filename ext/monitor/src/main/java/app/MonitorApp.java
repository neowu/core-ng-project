package app;

import core.framework.module.App;
import core.framework.module.SystemModule;

/**
 * @author ericchung
 */
public class MonitorApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));
        loadProperties("app.properties");

        load(new AlertModule());
        load(new MonitorModule());
    }
}
