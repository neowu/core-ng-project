package app.monitor.job;

import core.framework.internal.web.api.APIMessageDefinitionResponse;
import core.framework.json.JSON;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIMessageValidatorIntegrationTest {
    @Test
    void noChange() {
        var validator = new APIMessageValidator(response("api-message-validator-test/previous.json"),
            response("api-message-validator-test/previous.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isNull();
    }

    @Test
    void rename() {
        var validator = new APIMessageValidator(response("api-message-validator-test/previous.json"),
            response("api-message-validator-test/rename.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("WARN");
        assertThat(warnings.warnings)
            .containsExactly("renamed message type of customer-updated from CustomerUpdatedMessage to CustomerUpdatedMessageV2");
    }

    @Test
    void removeNotNullField() {
        var validator = new APIMessageValidator(response("api-message-validator-test/previous.json"),
            response("api-message-validator-test/remove-not-null-field.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("ERROR");
        assertThat(warnings.errors)
            .containsExactly("removed @NotNull from field CustomerUpdatedMessage.id",
                "removed field CustomerUpdatedMessage.name");
        assertThat(warnings.warnings).isEmpty();
    }


    private APIMessageDefinitionResponse response(String path) {
        return JSON.fromJSON(APIMessageDefinitionResponse.class, ClasspathResources.text(path));
    }
}
