package app.monitor.job;

import core.framework.internal.web.api.APIDefinitionV2Response;
import core.framework.json.JSON;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIValidatorTest {
    @Test
    void rename() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/rename.json"));
        String result = validator.validate();
        assertThat(result).isEqualTo("WARN");
        assertThat(validator.warnings)
            .containsExactly("renamed method CustomerAJAXService.get to NewCustomerAJAXService.newGet",
                "renamed method CustomerAJAXService.create to NewCustomerAJAXService.create",
                "renamed request type of CustomerAJAXService.create from RegisterAJAXRequest to NewRegisterAJAXRequest");
    }

    @Test
    void addNotNullField() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/add-not-null-field.json"));
        String result = validator.validate();
        assertThat(result).isEqualTo("ERROR");
        assertThat(validator.warnings)
            .containsExactly("added field GetCustomerAJAXResponse.group");
        assertThat(validator.errors)
            .containsExactly("added field @NotNull RegisterAJAXRequest.group");
    }

    @Test
    void changeFieldType() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/change-field-type.json"));
        String result = validator.validate();
        assertThat(result).isEqualTo("ERROR");
        assertThat(validator.warnings).isEmpty();
        assertThat(validator.errors)
            .containsExactly("changed field type of GetCustomerAJAXResponse.name from String to List<String>");
    }

    @Test
    void changeResponseType() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/change-response-type.json"));
        String result = validator.validate();
        assertThat(result).isEqualTo("ERROR");
        assertThat(validator.warnings).isEmpty();
        assertThat(validator.errors)
            .containsExactly("changed response type of CustomerAJAXService.get from GetCustomerAJAXResponse to Optional<GetCustomerAJAXResponse>",
                "changed response type of CustomerAJAXService.create from void to GetCustomerAJAXResponse");
    }

    @Test
    void deprecateMethod() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/deprecate-method.json"));
        String result = validator.validate();
        assertThat(result).isEqualTo("WARN");
        assertThat(validator.warnings)
            .containsExactly("added @Deprecated to method CustomerAJAXService.get",
                "added method CustomerAJAXService.getV2");
    }

    private APIDefinitionV2Response response(String path) {
        return JSON.fromJSON(APIDefinitionV2Response.class, ClasspathResources.text(path));
    }
}
