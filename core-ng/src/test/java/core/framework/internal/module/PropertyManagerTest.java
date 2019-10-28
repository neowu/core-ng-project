package core.framework.internal.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(propertyManager.property("sys.cache.host")).hasValue("overrideHost");

        System.clearProperty("sys.cache.host");
        assertThat(propertyManager.property("sys.cache.host")).hasValue("host");
    }

    @Test
    void overridePropertyWithEmptyValue() {
        System.setProperty("sys.cache.host", "overrideHost");
        propertyManager.properties.set("sys.cache.host", "");

        assertThat(propertyManager.property("sys.cache.host")).hasValue("overrideHost");
    }

    @Test
    void ignoreEnvIfKeyNotDefinedInProperties() {
        System.setProperty("sys.cache.host", "overrideHost");

        assertThat(propertyManager.property("sys.cache.host")).isEmpty();
    }

    @Test
    void maskValue() {
        assertThat(propertyManager.maskValue("sys.jdbc.password", "password")).doesNotContain("password");
        assertThat(propertyManager.maskValue("app.key.secret", "secret")).doesNotContain("secret");
        assertThat(propertyManager.maskValue("sys.jdbc.user", "user")).isEqualTo("user");

        assertThat(propertyManager.maskValue("SYS_JDBC_PASSWORD", "password")).doesNotContain("password");
    }

    @Test
    void envVarName() {
        assertThat(propertyManager.envVarName("sys.kafka.uri")).isEqualTo("SYS_KAFKA_URI");
    }
}
