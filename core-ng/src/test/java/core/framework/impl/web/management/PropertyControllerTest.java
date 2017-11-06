package core.framework.impl.web.management;

import core.framework.util.Properties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class PropertyControllerTest {
    @Test
    void text() {
        Properties properties = new Properties();
        properties.load("property-controller-test/test.properties");
        PropertyController controller = new PropertyController(properties);
        assertEquals("sys.jdbc.password=(masked)\nsys.jdbc.user=user\n", controller.text());
    }
}
