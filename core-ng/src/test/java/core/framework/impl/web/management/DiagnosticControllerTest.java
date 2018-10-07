package core.framework.impl.web.management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class DiagnosticControllerTest {
    private DiagnosticController controller;

    @BeforeEach
    void createJVMDiagnosticController() {
        controller = new DiagnosticController();
    }

    @Test
    void invoke() {
        String result = controller.invoke("vmFlags");
        assertThat(result).isNotEmpty();
    }
}
