package core.framework.internal.scheduler;

import java.io.Serial;

/**
 * @author neo
 */
public class JobException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2068063088454403332L;

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }
}
