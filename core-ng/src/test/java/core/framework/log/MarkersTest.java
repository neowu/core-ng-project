package core.framework.log;

import org.junit.jupiter.api.Test;
import org.slf4j.Marker;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class MarkersTest {
    @Test
    void errorCode() {
        Marker marker = Markers.errorCode("ERROR");
        assertEquals("ERROR", marker.getName());
    }
}
