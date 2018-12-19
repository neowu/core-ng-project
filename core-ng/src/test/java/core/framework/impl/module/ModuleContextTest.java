package core.framework.impl.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ModuleContextTest {
    private ModuleContext moduleContext;

    @BeforeEach
    void createModuleContext() {
        moduleContext = new ModuleContext();
    }

    @Test
    void validate() {
        moduleContext.propertyManager.properties.set("app.notUsedKey", "value");
        moduleContext.visitedProperties.add("app.usedKey");

        assertThatThrownBy(() -> moduleContext.validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("found not used property")
                .hasMessageContaining("key=app.notUsedKey");
    }
}
