package core.diagram.service;

import core.log.domain.ActionDocument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author neo
 */
class ActionDiagramTest {
    @Test
    void key() {
        var key1 = new ActionDiagram.Key("app", "action");
        var key2 = new ActionDiagram.Key("app", "action");

        assertThat(key1)
            .isNotSameAs(key2)
            .isEqualTo(key2)
            .hasSameHashCodeAs(key2);
    }

    @Test
    void keyId() {
        var key = new ActionDiagram.Key("app", "action");
        assertThat(key.id()).isEqualTo("app_action");
    }

    @Test
    void tooltip() {
        var diagram = new ActionDiagram();
        var doc = new ActionDocument();
        doc.id = "id";
        doc.result = "WARN";
        doc.errorCode = "ERROR_CODE";
        doc.context = Map.of("controller", List.of("controllerValue"));
        String tooltip = diagram.tooltip("action", "app", List.of(doc));

        assertThat(tooltip)
            .contains("<tr><td>controller</td><td>controllerValue</td></tr>")
            .contains("<tr style='color:OrangeRed'><td>id</td><td>ERROR_CODE</td></tr>");
    }
}
