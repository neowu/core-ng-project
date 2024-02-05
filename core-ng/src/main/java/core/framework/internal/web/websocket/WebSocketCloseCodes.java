package core.framework.internal.web.websocket;

import core.framework.web.exception.BadRequestException;
import core.framework.web.exception.TooManyRequestsException;
import core.framework.web.exception.UnauthorizedException;

/**
 * @author neo
 */
public final class WebSocketCloseCodes {
    // refer to https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent/code
    public static final int NORMAL_CLOSURE = 1000;
    public static final int ABNORMAL_CLOSURE = 1006;
    public static final int POLICY_VIOLATION = 1008;
    public static final int INTERNAL_ERROR = 1011;
    public static final int SERVICE_RESTART = 1012;
    public static final int TRY_AGAIN_LATER = 1013;

    // as websocket does not have restful convention, here only supports general cases
    static int closeCode(Throwable e) {
        if (e instanceof TooManyRequestsException) return TRY_AGAIN_LATER;
        if (e instanceof BadRequestException) return POLICY_VIOLATION;
        if (e instanceof UnauthorizedException) return POLICY_VIOLATION;
        return INTERNAL_ERROR;
    }
}
