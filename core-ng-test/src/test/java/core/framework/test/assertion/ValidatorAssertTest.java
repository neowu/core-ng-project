package core.framework.test.assertion;

import core.framework.api.validate.NotBlank;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;
import org.junit.jupiter.api.Test;

import static core.framework.test.Assertions.assertBean;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ValidatorAssertTest {
    @Test
    void failWithMessage() {
        var bean = new Bean();
        bean.field2 = "";

        assertThatThrownBy(() -> assertBean(bean).isValid())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("to be valid, but found some violations:")
                .hasMessageContaining("{field1=field must not be null, field2=field must not be blank}");
    }

    @Test
    void isValid() {
        var bean = new Bean();
        bean.field1 = "value";
        assertBean(bean).isValid();
    }

    @Test
    void hasError() {
        var bean = new Bean();
        bean.field1 = "123456";
        assertBean(bean).errors().containsEntry("field1", "field1 must not be longer than 5");
    }

    public static class Bean {
        @NotNull
        @Size(max = 5, message = "field1 must not be longer than 5")
        public String field1;

        @NotBlank
        public String field2;
    }
}
