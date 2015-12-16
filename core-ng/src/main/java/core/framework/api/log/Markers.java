package core.framework.api.log;

import core.framework.impl.log.marker.ErrorTypeMarker;
import org.slf4j.Marker;

/**
 * @author neo
 */
public final class Markers {
    public static Marker errorType(String errorType) {
        return new ErrorTypeMarker(errorType);
    }
}
