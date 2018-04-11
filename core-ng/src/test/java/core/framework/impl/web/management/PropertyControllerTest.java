package core.framework.impl.web.management;

import core.framework.impl.module.PropertyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class PropertyControllerTest {
    private PropertyController controller;
    private PropertyManager propertyManager;

    @BeforeEach
    void createPropertyController() {
        propertyManager = new PropertyManager();
        controller = new PropertyController(propertyManager);
    }

    @Test
    void text() {
        propertyManager.properties.set("sys.jdbc.user", "user");
        propertyManager.properties.set("sys.jdbc.password", "password");

        assertThat(controller.text())
                .contains("sys.jdbc.password=******")
                .contains("sys.jdbc.user=user");
    }
}
