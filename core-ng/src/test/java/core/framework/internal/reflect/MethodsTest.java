package core.framework.internal.reflect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MethodsTest {
    String testMethod(String param1, Integer param2, Long param3) {
        return null;
    }

    @Test
    void path() throws NoSuchMethodException {
        String path = Methods.path(MethodsTest.class.getDeclaredMethod("testMethod", String.class, Integer.class, Long.class));
        assertThat(path).isEqualTo("core.framework.internal.reflect.MethodsTest.testMethod(String, Integer, Long)");
    }
}
