package core.framework.api.log;

/**
 * @author neo
 */
public interface ErrorCode {
    String errorCode();

    default Severity severity() {
        return Severity.ERROR;
    }
}
