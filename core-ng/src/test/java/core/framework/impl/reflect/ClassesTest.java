package core.framework.impl.reflect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
