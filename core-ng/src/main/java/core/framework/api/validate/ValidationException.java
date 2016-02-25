package core.framework.api.validate;

import core.framework.api.log.Warning;
import core.framework.api.util.Strings;

import java.util.Map;

/**
 * @author neo
 */
@Warning
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 9215299700445046388L;
    public final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super(Strings.format("validation failed, error={}", errors));
        this.errors = errors;
    }
}
