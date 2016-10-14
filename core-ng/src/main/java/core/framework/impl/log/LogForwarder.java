package core.framework.impl.log;

import java.util.Map;

/**
 * @author neo
 */
public interface LogForwarder {
    void start();

    void stop();

    void forwardLog(ActionLog log);

    void forwardStats(Map<String, Double> stats);
}
