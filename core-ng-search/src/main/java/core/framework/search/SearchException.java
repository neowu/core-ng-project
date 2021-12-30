package core.framework.search;

import java.io.Serial;

/**
 * @author neo
 */
public class SearchException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -1161076239987892452L;

    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
