package core.framework.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        properties.set("key", "");

        assertThat(properties.get("key")).isEmpty();
    }

    @Test
    void loadNotExistedFile() {
        assertThatThrownBy(() -> properties.load("not-existed-property.properties"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("can not load");
    }

    @Test
    void setWithDuplicateKey() {
        assertThatThrownBy(() -> {
            properties.set("key1", "value1");
            properties.set("key1", "value2");
        }).isInstanceOf(Error.class)
          .hasMessageContaining("key=key1, previous=value1, current=value2");
    }

    @Test
    void containsKey() {
        properties.properties.put("key", "");

        assertThat(properties.containsKey("key")).isTrue();
    }
}
