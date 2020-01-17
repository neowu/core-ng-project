package core.log;

import core.framework.test.module.AbstractTestModule;
import core.log.service.IndexService;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        load(new LogProcessorApp());

        bean(IndexService.class).createIndexTemplates();
    }
}
