package core.framework.internal.async;

import java.io.Serial;

/**
 * @author neo
 */
public class TaskException extends Exception {
    @Serial
    private static final long serialVersionUID = -5143310703118077256L;

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
