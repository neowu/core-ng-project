package core.log;

import core.framework.api.AbstractTestModule;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        load(new LogProcessorApp());

        initSearch().createIndexTemplate("action", "action-index-template.json");
        initSearch().createIndexTemplate("trace", "trace-index-template.json");
        initSearch().createIndexTemplate("stat", "stat-index-template.json");
    }
}
