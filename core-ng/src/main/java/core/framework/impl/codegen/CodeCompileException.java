package core.framework.impl.codegen;

/**
 * @author neo
 */
public class CodeCompileException extends RuntimeException {
    private static final long serialVersionUID = -8184850154210701717L;

    public CodeCompileException(String message) {
        super(message);
    }

    public CodeCompileException(Throwable cause) {
        super(cause);
    }
}
