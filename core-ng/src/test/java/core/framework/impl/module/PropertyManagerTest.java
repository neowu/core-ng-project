package core.framework.impl.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author neo
 */
class PropertyManagerTest {
    private PropertyManager propertyManager;

    @BeforeEach
    void createPropertyManager() {
        propertyManager = new PropertyManager();
        System.clearProperty("sys.cache.host");
    }

    @Test
    void property() {
        System.setProperty("sys.cache.host", "overrideHost");
        propertyManager.properties.set("sys.cache.host", "host");

        assertEquals("overrideHost", propertyManager.property("sys.cache.host").orElse(""));

        System.clearProperty("sys.cache.host");
        assertEquals("host", propertyManager.property("sys.cache.host").orElse(""));
    }

    @Test
    void maskValue() {
        assertNotEquals("password", propertyManager.maskValue("sys.jdbc.password", "password"));
        assertNotEquals("secret", propertyManager.maskValue("app.key.secret", "secret"));
        assertEquals("user", propertyManager.maskValue("sys.jdbc.user", "user"));
    }

    @Test
    void envVarName() {
        assertEquals("SYS_KAFKA_URI", propertyManager.envVarName("sys.kafka.uri"));
    }
}
