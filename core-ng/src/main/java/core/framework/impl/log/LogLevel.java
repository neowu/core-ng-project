package core.framework.impl.log;

/**
 * @author neo
 */
enum LogLevel {
    ERROR(4), WARN(3), INFO(2), DEBUG(1);

    final int value;

    LogLevel(int value) {
        this.value = value;
    }
}
