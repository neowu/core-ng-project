package core.log;

import core.framework.api.AbstractTestModule;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        load(new LogProcessorApp());

        initSearch().createIndex("action", "action-index.json");
        initSearch().createIndex("trace", "trace-index.json");
    }
}
