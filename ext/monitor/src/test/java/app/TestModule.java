package app;

import core.framework.test.module.AbstractTestModule;
import core.framework.util.ClasspathResources;

public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        System.setProperty("app.alert.config", ClasspathResources.text("alert.json"));
        System.setProperty("app.monitor.config", ClasspathResources.text("monitor.json"));

        load(new MonitorApp());
    }
}
