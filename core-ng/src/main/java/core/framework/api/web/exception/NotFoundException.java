package core.framework.api.web.exception;

import core.framework.api.log.Warning;

/**
 * @author neo
 */
@Warning
public class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 8663360723004690205L;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }
}
