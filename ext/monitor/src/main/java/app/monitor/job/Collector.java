package app.monitor.job;

import core.framework.internal.stat.Stats;

/**
 * @author neo
 */
public interface Collector {
    Stats collect();
}
