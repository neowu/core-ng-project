package core.framework.module;

import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public class TestSessionConfig extends SessionConfig {
    TestSessionConfig(ModuleContext context) {
        super(context);
    }

    @Override
    public void redis(String host) {
        local();
    }
}
