package core.framework.impl.reflect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MethodsTest {
    String testMethod(String param1, Integer param2) {
        return null;
    }

    @Test
    void path() throws NoSuchMethodException {
        String path = Methods.path(MethodsTest.class.getDeclaredMethod("testMethod", String.class, Integer.class));
        assertThat(path).isEqualTo("core.framework.impl.reflect.MethodsTest.testMethod(java.lang.String, java.lang.Integer)");
    }
}
