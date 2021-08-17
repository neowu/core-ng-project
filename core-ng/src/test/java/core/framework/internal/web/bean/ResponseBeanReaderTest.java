package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.bean.TestBean;
import core.framework.internal.validate.ValidationException;
import core.framework.internal.web.service.InternalErrorResponse;
import core.framework.json.JSON;
import core.framework.util.Strings;
import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ResponseBeanReaderTest {
    private ResponseBeanReader reader;
    private BeanClassValidator validator;

    @BeforeEach
    void createResponseBeanMapper() {
        validator = new BeanClassValidator();
        reader = new ResponseBeanReader();
        reader.register(TestBean.class, validator);
    }

    @Test
    void register() {
        reader.register(TestBean.class, validator);
    }

    @Test
    void builtinClasses() {
        // webservice client may get error response
        assertThat(reader.context).containsKey(InternalErrorResponse.class);
    }

    @Test
    void fromJSONWithEmptyOptional() throws IOException {
        @SuppressWarnings("unchecked")
        var parsedBean = (Optional<TestBean>) reader.fromJSON(Types.optional(TestBean.class), Strings.bytes("null"));
        assertThat(parsedBean).isNotPresent();
    }

    @Test
    void fromJSONWithOptional() throws IOException {
        var bean = new TestBean();
        bean.intField = 3;

        @SuppressWarnings("unchecked")
        var parsedBean = (Optional<TestBean>) reader.fromJSON(Types.optional(TestBean.class), Strings.bytes(JSON.toJSON(bean)));
        assertThat(parsedBean).get().usingRecursiveComparison().isEqualTo(bean);
    }

    @Test
    void fromJSONWithValidationError() {
        var bean = new TestBean();

        assertThatThrownBy(() -> reader.fromJSON(TestBean.class, Strings.bytes(JSON.toJSON(bean))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("validation failed");
    }

    @Test
    void fromJSONWithVoid() throws IOException {
        assertThat(reader.fromJSON(void.class, null)).isNull();
    }

    @Test
    void fromJSON() throws IOException {
        var bean = new TestBean();
        bean.intField = 3;

        TestBean parsedBean = (TestBean) reader.fromJSON(TestBean.class, Strings.bytes(JSON.toJSON(bean)));
        assertThat(parsedBean).usingRecursiveComparison().isEqualTo(bean);
    }
}
