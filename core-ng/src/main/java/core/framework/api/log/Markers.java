package core.framework.api.log;

import core.framework.impl.log.marker.ErrorTypeMarker;
import core.framework.impl.log.marker.TraceMarker;

/**
 * @author neo
 */
public final class Markers {
    public static final TraceMarker TRACE = new TraceMarker();

    public static ErrorTypeMarker errorType(String errorType) {
        return new ErrorTypeMarker(errorType);
    }
}
