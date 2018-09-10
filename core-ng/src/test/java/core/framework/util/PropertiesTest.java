package core.framework.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class PropertiesTest {
    private Properties properties;

    @BeforeEach
    void createProperties() {
        properties = new Properties();
    }

    @Test
    void testGetEmptyValue() {
        properties.set("key", "");

        assertFalse(properties.get("key").isPresent());
    }

    @Test
    void loadNotExistedFile() {
        Error error = assertThrows(Error.class, () -> properties.load("not-existed-property.properties"));
        assertThat(error.getMessage()).contains("can not find");
    }

    @Test
    void setWithDuplicateKey() {
        Error error = assertThrows(Error.class, () -> {
            properties.set("key1", "value1");
            properties.set("key1", "value2");
        });
        assertThat(error.getMessage()).contains("key=key1, previous=value1, current=value2");
    }

    @Test
    void containsKey() {
        properties.properties.put("key", "");

        assertTrue(properties.containsKey("key"));
    }
}
