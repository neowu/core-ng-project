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
    void maskedValue() {
        PropertyManager.PropertyEntry entry = new PropertyManager.PropertyEntry("sys.jdbc.password", "password", false);
        assertNotEquals("password", entry.maskedValue());

        entry = new PropertyManager.PropertyEntry("app.key.secret", "secret", false);
        assertNotEquals("secret", entry.maskedValue());

        entry = new PropertyManager.PropertyEntry("sys.jdbc.user", "user", false);
        assertEquals("user", entry.maskedValue());
    }
}
