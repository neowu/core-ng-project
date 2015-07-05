package core.framework.api.web.exception;

import core.framework.api.exception.Warning;

import java.util.Map;

/**
 * @author neo
 */
@Warning
public class ValidationException extends Exception {
    private static final long serialVersionUID = 9215299700445046388L;
    public final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }
}
