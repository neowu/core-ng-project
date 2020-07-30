package core.framework.internal.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ModuleContextTest {
    private ModuleContext moduleContext;

    @BeforeEach
    void createModuleContext() {
        moduleContext = new ModuleContext(null);
    }

    @Test
    void property() {
        moduleContext.property("app.key");
        assertThat(moduleContext.propertyValidator.usedProperties).contains("app.key");
    }
}
