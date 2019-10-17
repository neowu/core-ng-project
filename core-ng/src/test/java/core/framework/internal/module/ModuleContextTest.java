package core.framework.internal.module;

import core.framework.internal.log.LogManager;
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
        moduleContext = new ModuleContext(new LogManager());
    }

    @Test
    void property() {
        moduleContext.property("app.key");
        assertThat(moduleContext.propertyValidator.usedProperties).contains("app.key");
    }
}
