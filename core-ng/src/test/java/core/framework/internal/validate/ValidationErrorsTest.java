package core.framework.internal.validate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ValidationErrorsTest {
    private ValidationErrors errors;

    @BeforeEach
    void createValidationErrors() {
        errors = new ValidationErrors();
    }

    @Test
    void message() {
        assertThat(errors.message("{value} must not larger than {max}", Map.of("value", "1", "max", "0")))
                .isEqualTo("1 must not larger than 0");

        assertThat(errors.message("{value}", Map.of()))
                .isEqualTo("{value}");

        assertThat(errors.message("{value", Map.of("value", "1")))
                .isEqualTo("{value");

        assertThat(errors.message("{value}", Map.of("value", "1", "max", "100")))
                .isEqualTo("1");
    }
}
