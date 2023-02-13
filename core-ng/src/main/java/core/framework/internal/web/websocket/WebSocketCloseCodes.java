package core.framework.internal.web.websocket;

/**
 * @author neo
 */
public final class WebSocketCloseCodes {
    // refer to https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent/code
    public static final int NORMAL_CLOSURE = 1000;
    public static final int POLICY_VIOLATION = 1008;
    public static final int INTERNAL_ERROR = 1011;
    public static final int SERVICE_RESTART = 1012;
    public static final int TRY_AGAIN_LATER = 1013;
}
