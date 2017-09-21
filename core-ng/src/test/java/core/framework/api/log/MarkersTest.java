package core.framework.api.log;

import org.junit.Test;
import org.slf4j.Marker;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class MarkersTest {
    @Test
    public void errorCode() {
        Marker marker = Markers.errorCode("ERROR");
        assertEquals("ERROR", marker.getName());
    }
}
