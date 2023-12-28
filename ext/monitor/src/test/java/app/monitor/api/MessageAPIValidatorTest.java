package app.monitor.api;

import core.framework.internal.web.api.MessageAPIDefinitionResponse;
import core.framework.json.JSON;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MessageAPIValidatorTest {
    @Test
    void noChange() {
        var validator = new MessageAPIValidator(response("message-api-validator-test/previous.json"),
            response("message-api-validator-test/previous.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isNull();
    }

    @Test
    void renameMessageType() {
        var validator = new MessageAPIValidator(response("message-api-validator-test/previous.json"),
            response("message-api-validator-test/rename-message-type.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("WARN");
        assertThat(warnings.warnings)
            .containsExactly("renamed message type of customer-updated from CustomerUpdatedMessage to CustomerUpdatedMessageV2");
    }

    @Test
    void removeNotNullField() {
        var validator = new MessageAPIValidator(response("message-api-validator-test/previous.json"),
            response("message-api-validator-test/remove-not-null-field.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("ERROR");
        assertThat(warnings.errors)
            .containsExactly("removed @NotNull from field CustomerUpdatedMessage.id",
                "removed field CustomerUpdatedMessage.name");
        assertThat(warnings.warnings).isEmpty();
    }

    @Test
    void addNotNullField() {
        var validator = new MessageAPIValidator(response("message-api-validator-test/previous.json"),
            response("message-api-validator-test/add-not-null-field.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("WARN");
        assertThat(warnings.warnings)
            .containsExactly("added @NotNull to field CustomerUpdatedMessage.address",
                "added field CustomerUpdatedMessage.other");
    }

    @Test
    void renameTopic() {
        var validator = new MessageAPIValidator(response("message-api-validator-test/previous.json"),
            response("message-api-validator-test/rename-topic.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("WARN");
        assertThat(warnings.warnings)
            .containsExactly("removed message publisher, topic=customer-updated",
                "added message publisher, topic=customer-updated-v2");
    }

    @Test
    void removeTopic() {
        var validator = new MessageAPIValidator(response("message-api-validator-test/previous.json"),
            response("message-api-validator-test/remove-topic.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("WARN");
        assertThat(warnings.warnings)
            .containsExactly("removed message publisher, topic=customer-updated",
                "removed type Address",
                "removed type CustomerUpdatedMessage");
    }

    private MessageAPIDefinitionResponse response(String path) {
        return JSON.fromJSON(MessageAPIDefinitionResponse.class, ClasspathResources.text(path));
    }
}
