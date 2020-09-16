package core.framework.internal.async;

/**
 * @author neo
 */
public class TaskException extends Exception {
    private static final long serialVersionUID = -5143310703118077256L;

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
