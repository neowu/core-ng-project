package core.framework.json;

import java.io.Serial;

// json input is invalid
public class JSONException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 9098858367692044462L;

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
