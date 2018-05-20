package core.framework.module;

/**
 * @author neo
 */
public class TestSessionConfig extends SessionConfig {
    @Override
    public void redis(String host) {
        local();
    }
}
