package core.diagram.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ArchDiagramTest {
    private ArchDiagram diagram;

    @BeforeEach
    void createArchDiagram() {
        diagram = new ArchDiagram(Set.of());
    }

    @Test
    void messageSubscriptionTooltip() {
        var subscription = new ArchDiagram.MessageSubscription("topic");
        subscription.consumers.put("consumer", 10L);
        subscription.publishers.put("publisher", 5L);
        String tooltip = diagram.tooltip(subscription);

        assertThat(tooltip)
            .contains("<caption>topic</caption>")
            .contains("<tr><td colspan=2 class=section>publishers</td><tr>")
            .contains("<tr><td>publisher</td><td>5</td></tr>");
    }
}
