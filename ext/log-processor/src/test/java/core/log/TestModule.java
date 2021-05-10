package core.log;

import core.LogProcessorApp;
import core.framework.test.module.AbstractTestModule;
import core.framework.util.ClasspathResources;
import core.log.service.IndexService;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        System.setProperty("app.log.forward.config", ClasspathResources.text("forward.json"));
        load(new LogProcessorApp());

        bean(IndexService.class).createIndexTemplates();
    }
}
