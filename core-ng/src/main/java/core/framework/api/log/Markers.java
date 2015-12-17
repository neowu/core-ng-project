package core.framework.api.log;

import core.framework.impl.log.marker.ErrorCodeMarker;
import org.slf4j.Marker;

/**
 * @author neo
 */
public final class Markers {
    public static Marker errorCode(String code) {
        return new ErrorCodeMarker(code);
    }
}
