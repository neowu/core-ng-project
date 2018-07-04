package core.framework.impl.web.bean;

import core.framework.util.Charsets;
import core.framework.util.Lists;
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
        Optional<TestBean> optional = Optional.of(new TestBean());
        byte[] bytes = responseBeanMapper.toJSON(optional);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    void registerBean() {
        responseBeanMapper.register(TestBean.class);
    }
}
