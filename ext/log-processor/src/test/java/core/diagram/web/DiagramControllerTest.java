package core.diagram.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DiagramControllerTest {
    private DiagramController controller;

    @BeforeEach
    void createDiagramController() {
        controller = new DiagramController();
    }

    @Test
    void intParams() {
        assertThat(controller.intParam(Map.of("key", "12"), "key", 0)).isEqualTo(12);
        assertThat(controller.intParam(Map.of(), "key", 24)).isEqualTo(24);
    }

    @Test
    void listParams() {
        assertThat(controller.listParam(Map.of("key", "a,b,c"), "key")).contains("a", "b", "c");
        assertThat(controller.listParam(Map.of(), "key")).isEmpty();
    }
}
