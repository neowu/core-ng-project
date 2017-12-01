package core.framework.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void getEmptyValue() {
        properties.properties.put("key", "");

        assertFalse(properties.get("key").isPresent());
    }

    @Test
    void loadNotExistedFile() {
        Error error = assertThrows(Error.class, () -> properties.load("not-existed-property.properties"));
        assertThat(error.getMessage()).contains("can not find");
    }
}
