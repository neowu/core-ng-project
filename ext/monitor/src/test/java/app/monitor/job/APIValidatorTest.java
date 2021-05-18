package app.monitor.job;

import core.framework.internal.web.api.APIDefinitionV2Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIValidatorTest {
    private APIValidator validator;

    @BeforeEach
    void createAPIValidator() {
        var response = new APIDefinitionV2Response();
        response.services = List.of();
        response.types = List.of();
        validator = new APIValidator(response, response);
    }

    @Test
    void errorMessage() {
        validator.errors.add("error1");
        validator.errors.add("error2");
        validator.warnings.add("warning1");

        String message = validator.errorMessage();
        assertThat(message)
            .isEqualToIgnoringNewLines("""
                *incompatible changes*
                * error1
                * error2
                *compatible changes*
                * warning1""");
    }
}
