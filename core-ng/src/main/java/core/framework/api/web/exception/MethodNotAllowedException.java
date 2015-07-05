package core.framework.api.web.exception;

import core.framework.api.exception.Warning;
import core.framework.api.http.HTTPMethod;

/**
 * @author neo
 */
@Warning
public class MethodNotAllowedException extends RuntimeException {
    private static final long serialVersionUID = 2349080664326196294L;

    public MethodNotAllowedException(HTTPMethod method) {
        super("method not allowed, method=" + method);
    }
}
