package core.framework.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SetsTest {
    @Test
    void newEnumSet() {
        Set<TestEnum> set = Sets.newEnumSet(TestEnum.class);
        set.add(TestEnum.A);
        assertThat(set).containsOnly(TestEnum.A);
    }

    enum TestEnum {
        A, B
    }
}
