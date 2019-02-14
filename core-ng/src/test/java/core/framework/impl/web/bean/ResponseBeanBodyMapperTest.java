package core.framework.impl.web.bean;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.validate.ValidationException;
import core.framework.util.Lists;
import core.framework.util.Strings;
import core.framework.util.Types;
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
class ResponseBeanBodyMapperTest {
    private ResponseBeanMapper responseBeanMapper;
    private JSONMapper<TestBean> mapper;

    @BeforeEach
    void createResponseBeanMapper() {
        responseBeanMapper = new ResponseBeanMapper(new BeanBodyMapperRegistry());
        mapper = new JSONMapper<>(TestBean.class);
    }

    @Test
    void validateList() {
        List<TestBean> list = Lists.newArrayList();
        assertThatThrownBy(() -> responseBeanMapper.toJSON(list))
                .isInstanceOf(Error.class)
                .hasMessageContaining("class must be bean class");
    }

    @Test
    void testToJSONWithEmptyOptional() {
        Optional<TestBean> optional = Optional.empty();
        byte[] bytes = responseBeanMapper.toJSON(optional);
        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("null");
    }

    @Test
    void testToJSONWithOptional() {
        TestBean bean = new TestBean();
        bean.intField = 5;
        Optional<TestBean> optional = Optional.of(bean);
        byte[] bytes = responseBeanMapper.toJSON(optional);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    void testToJSONWithValidationError() {
        assertThatThrownBy(() -> responseBeanMapper.toJSON(new TestBean()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void registerBean() {
        responseBeanMapper.register(TestBean.class);
    }

    @Test
    void fromJSONWithEmptyOptional() {
        @SuppressWarnings("unchecked")
        var parsedBean = (Optional<TestBean>) responseBeanMapper.fromJSON(Types.optional(TestBean.class), Strings.bytes("null"));
        assertThat(parsedBean).isNotPresent();
    }

    @Test
    void fromJSONWithOptional() {
        var bean = new TestBean();
        bean.intField = 3;

        @SuppressWarnings("unchecked")
        var parsedBean = (Optional<TestBean>) responseBeanMapper.fromJSON(Types.optional(TestBean.class), mapper.toJSON(bean));
        assertThat(parsedBean).get().isEqualToComparingFieldByField(bean);
    }

    @Test
    void fromJSONWithValidationError() {
        var bean = new TestBean();

        assertThatThrownBy(() -> responseBeanMapper.fromJSON(TestBean.class, mapper.toJSON(bean)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("validation failed");
    }

    @Test
    void fromJSONWithVoid() {
        assertThat(responseBeanMapper.fromJSON(void.class, null)).isNull();
    }

    @Test
    void fromJSON() {
        TestBean bean = new TestBean();
        bean.intField = 3;

        TestBean parsedBean = (TestBean) responseBeanMapper.fromJSON(TestBean.class, mapper.toJSON(bean));
        assertThat(parsedBean).isEqualToComparingFieldByField(bean);
    }
}
