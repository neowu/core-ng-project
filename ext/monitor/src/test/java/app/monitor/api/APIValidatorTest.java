package app.monitor.api;

import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.json.JSON;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIValidatorTest {
    @Test
    void noChange() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/previous.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isNull();
    }

    @Test
    void rename() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/rename.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("WARN");
        assertThat(warnings.warnings)
            .containsExactly("renamed method CustomerAJAXService.get to NewCustomerAJAXService.newGet",
                "renamed method CustomerAJAXService.create to NewCustomerAJAXService.create",
                "renamed request type of CustomerAJAXService.create from RegisterAJAXRequest to NewRegisterAJAXRequest");
    }

    @Test
    void addNotNullField() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/add-not-null-field.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("ERROR");
        assertThat(warnings.warnings)
            .containsExactly("added field GetCustomerAJAXResponse.group");
        assertThat(warnings.errors)
            .containsExactly("added field @NotNull RegisterAJAXRequest.group");
    }

    @Test
    void changeFieldType() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/change-field-type.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("ERROR");
        assertThat(warnings.warnings).isEmpty();
        assertThat(warnings.errors)
            .containsExactly("changed field type of GetCustomerAJAXResponse.name from String to List<String>",
                "changed field type of RegisterAJAXRequest.email from String to Map<String, GetCustomerAJAXResponse>");
    }

    @Test
    void changeResponseType() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/change-response-type.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("ERROR");
        assertThat(warnings.warnings).isEmpty();
        assertThat(warnings.errors)
            .containsExactly("changed response type of CustomerAJAXService.get from GetCustomerAJAXResponse to Optional<GetCustomerAJAXResponse>",
                "changed response type of CustomerAJAXService.create from void to GetCustomerAJAXResponse");
    }

    @Test
    void deprecateMethod() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/deprecate-method.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("WARN");
        assertThat(warnings.warnings)
            .containsExactly("added @Deprecated to method CustomerAJAXService.get",
                """
                    added method @GET @Path("/ajax/customer/:id/v2") CustomerAJAXService.getV2""",
                "added type GetCustomerAJAXResponseV2");
    }

    @Test
    void removeDeprecatedMethod() {
        var validator = new APIValidator(response("api-validator-test/deprecate-method.json"),
            response("api-validator-test/remove-deprecated-method.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("WARN");
        assertThat(warnings.warnings)
            .containsExactly("""
                    removed method @Deprecated @GET @Path("/ajax/customer/:id") CustomerAJAXService.get""",
                "removed type GetCustomerAJAXResponse");
    }

    @Test
    void removeMethod() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/remove-method.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("ERROR");
        assertThat(warnings.warnings).isEmpty();
        assertThat(warnings.errors)
            .containsExactly("""
                    removed method @GET @Path("/ajax/customer/:id") CustomerAJAXService.get""",
                "removed type GetCustomerAJAXResponse",
                "removed type ErrorCode",
                "removed type Address");
    }

    @Test
    void changeEnumValue() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/change-enum-values.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("ERROR");
        assertThat(warnings.errors)
            .containsExactly("changed enum value of ErrorCode.ERROR_1 from ERROR_1 to ERROR_1_CHANGED",
                "removed enum value ErrorCode.ERROR_2",
                "added enum value ErrorCode.ERROR_3");
    }

    @Test
    void removeField() {
        var validator = new APIValidator(response("api-validator-test/previous.json"),
            response("api-validator-test/remove-field.json"));
        APIWarnings warnings = validator.validate();
        assertThat(warnings.result()).isEqualTo("WARN");
        assertThat(warnings.warnings)
            .containsExactly("removed field GetCustomerAJAXResponse.address",
                "removed field RegisterAJAXRequest.password",
                "removed type Address");
    }

    private APIDefinitionResponse response(String path) {
        return JSON.fromJSON(APIDefinitionResponse.class, ClasspathResources.text(path));
    }
}
