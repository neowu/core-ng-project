package core.framework.internal.reflect;

import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class ClassesTest {
    @Test
    void className() {
        assertThat(Classes.className(TestBean.class)).isEqualTo("ClassesTest$TestBean");
    }

    public static class TestBean {
    }
}
