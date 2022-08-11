package core.framework.internal.web.api;

import core.framework.internal.kafka.TestMessage;
import core.framework.json.JSON;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MessageAPIDefinitionBuilderTest {
    private MessageAPIDefinitionBuilder builder;

    @BeforeEach
    void createAPIDefinitionBuilder() {
        builder = new MessageAPIDefinitionBuilder(Map.of("topic", TestMessage.class));
    }

    @Test
    void build() {
        MessageAPIDefinitionResponse response = builder.build();
        MessageAPIDefinitionResponse expectedResponse = JSON.fromJSON(MessageAPIDefinitionResponse.class, ClasspathResources.text("api-test/test-message.json"));
        assertThat(response).usingRecursiveComparison()
            .ignoringFields("version")
            .isEqualTo(expectedResponse);
    }
}
