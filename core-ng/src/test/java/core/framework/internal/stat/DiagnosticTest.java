package core.framework.internal.stat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class DiagnosticTest {
    @Test
    void nativeMemory() {
        assertThat(Diagnostic.nativeMemory()).isNotNull();
    }
}
