package core.framework.impl.web.bean;

import core.framework.impl.validate.ValidationException;
import core.framework.json.JSON;
import core.framework.util.Charsets;
import core.framework.util.Lists;
import core.framework.util.Strings;
import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ResponseBeanMapperTest {
    private ResponseBeanMapper responseBeanMapper;

    @BeforeEach
    void createResponseBeanMapper() {
        responseBeanMapper = new ResponseBeanMapper(new BeanClassNameValidator());
    }

    @Test
    void validateList() {
        List<TestBean> list = Lists.newArrayList();
        assertThatThrownBy(() -> responseBeanMapper.toJSON(list))
                .isInstanceOf(Error.class)
                .hasMessageContaining("top level list is not allowed");
    }

    @Test
    void toJSONWithEmptyOptional() {
        Optional<TestBean> optional = Optional.empty();
        byte[] bytes = responseBeanMapper.toJSON(optional);
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo("null");
    }

    @Test
    void toJSONWithOptional() {
        TestBean bean = new TestBean();
        bean.intField = 5;
        Optional<TestBean> optional = Optional.of(bean);
        byte[] bytes = responseBeanMapper.toJSON(optional);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    void toJSONWithValidationError() {
        assertThatThrownBy(() -> responseBeanMapper.toJSON(new TestBean()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void registerBean() {
        responseBeanMapper.register(TestBean.class);
    }

    @Test
    void fromJSONWithEmptyOptional() {
        Optional<TestBean> parsedBean = responseBeanMapper.fromJSON(Types.optional(TestBean.class), Strings.bytes("null"));
        assertThat(parsedBean).isNotPresent();
    }

    @Test
    void fromJSONWithOptional() {
        TestBean bean = new TestBean();
        bean.intField = 3;
        String json = JSON.toJSON(bean);

        Optional<TestBean> parsedBean = responseBeanMapper.fromJSON(Types.optional(TestBean.class), Strings.bytes(json));
        assertThat(parsedBean).get().isEqualToComparingFieldByField(bean);
    }

    @Test
    void fromJSON() {
        TestBean bean = new TestBean();
        bean.intField = 3;
        String json = JSON.toJSON(bean);

        TestBean parsedBean = responseBeanMapper.fromJSON(TestBean.class, Strings.bytes(json));
        assertThat(parsedBean).isEqualToComparingFieldByField(bean);
    }
}
