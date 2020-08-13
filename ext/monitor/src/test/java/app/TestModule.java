package app;

import app.monitor.job.KubeClient;
import core.framework.test.module.AbstractTestModule;
import core.framework.util.ClasspathResources;

import static org.mockito.Mockito.mock;

public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        System.setProperty("app.alert.config", ClasspathResources.text("alert.json"));
        System.setProperty("app.monitor.config", ClasspathResources.text("monitor.json"));

        overrideBinding(KubeClient.class, mock(KubeClient.class));
        load(new MonitorApp());
    }
}
