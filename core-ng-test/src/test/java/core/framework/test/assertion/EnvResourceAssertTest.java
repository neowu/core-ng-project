package core.framework.test.assertion;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class EnvResourceAssertTest {
    @Test
    void overridesDefaultResources() {
        var env = new EnvResourceAssert(Paths.get("src/test/resources/env/conf1"),
            Paths.get("src/test/resources/env/main"),
            Paths.get("src/test/resources/env/main"));
        env.overridesDefaultResources();
    }

    @Test
    void testResourcesNotOverride() {
        var env = new EnvResourceAssert(Paths.get("src/test/resources/env/conf1"),
            Paths.get("src/test/resources/env/main"),
            Paths.get("src/test/resources/env/test"));
        assertThatThrownBy(env::overridesDefaultResources)
            .isInstanceOf(AssertionFailedError.class)
            .hasMessageContaining("src/test/resources/env/test/test.properties must override src/test/resources/env/main/test.properties");
    }

    @Test
    void confResourcesNotOverride() {
        var env = new EnvResourceAssert(Paths.get("src/test/resources/env/conf2"),
            Paths.get("src/test/resources/env/main"),
            Paths.get("src/test/resources/env/test"));
        assertThatThrownBy(env::overridesDefaultResources)
            .isInstanceOf(AssertionFailedError.class)
            .hasMessageContaining("src/test/resources/env/conf2/dev/resources/test.properties must override src/test/resources/env/main/test.properties");
    }
}
