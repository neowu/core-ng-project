package core.framework.internal.log;

/**
 * @author neo
 */
public enum LogLevel {
    ERROR(4), WARN(3), INFO(2), DEBUG(1);

    final int value;

    LogLevel(int value) {
        this.value = value;
    }
}
