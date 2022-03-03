package core.framework.internal.async;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CallableTaskTest {
    @Test
    void taskClass() {
        assertThat(CallableTask.taskClass(() -> 1).getName()).startsWith(CallableTaskTest.class.getName());

        assertThat(CallableTask.taskClass(new CallableTask(() -> {
        })).getName()).startsWith(CallableTaskTest.class.getName());
    }
}
