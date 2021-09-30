package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.bean.TestBean;
import core.framework.internal.validate.ValidationException;
import core.framework.internal.web.service.ErrorResponse;
import core.framework.internal.web.service.InternalErrorResponse;
import core.framework.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ResponseBeanWriterTest {
    private ResponseBeanWriter writer;
    private BeanClassValidator validator;

    @BeforeEach
    void createResponseBeanMapper() {
        validator = new BeanClassValidator();
        writer = new ResponseBeanWriter();
        writer.register(TestBean.class, validator);
    }

    @Test
    void register() {
        writer.register(TestBean.class, validator);
    }

    @Test
    void builtinClasses() {
        // controller may return error responses
        assertThat(writer.contains(ErrorResponse.class)).isTrue();
        assertThat(writer.contains(InternalErrorResponse.class)).isTrue();
    }

    @Test
    void validateList() {
        List<TestBean> list = Lists.newArrayList();
        assertThatThrownBy(() -> writer.toJSON(list))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class must not be java built-in class");
    }

    @Test
    void toJSONWithEmptyOptional() {
        Optional<TestBean> optional = Optional.empty();
        byte[] bytes = writer.toJSON(optional);
        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("null");
    }

    @Test
    void toJSONWithOptional() {
        var bean = new TestBean();
        bean.intField = 5;
        Optional<TestBean> optional = Optional.of(bean);
        byte[] bytes = writer.toJSON(optional);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    void toJSON() {
        var bean = new TestBean();
        bean.intField = 5;
        byte[] bytes = writer.toJSON(bean);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    void toJSONWithValidationError() {
        assertThatThrownBy(() -> writer.toJSON(new TestBean()))
                .isInstanceOf(ValidationException.class);
    }
}
