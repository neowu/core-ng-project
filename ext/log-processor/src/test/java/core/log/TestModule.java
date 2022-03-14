package core.log;

import core.LogProcessorApp;
import core.framework.search.module.InitSearchConfig;
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

        config(InitSearchConfig.class); // init es client in order to call index service
        bean(IndexService.class).createIndexTemplates();
    }
}
