package app;

import core.framework.api.App;
import core.framework.api.module.SystemModule;

/**
 * @author neo
 */
public class DemoServiceApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));
        http().port(8081);
        load(new ProductModule());
        load(new JobModule());
    }
}
