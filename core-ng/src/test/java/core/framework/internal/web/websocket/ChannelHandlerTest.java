package core.framework.internal.web.websocket;

import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.web.bean.BeanMapper;
import core.framework.internal.web.bean.BeanMappers;
import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ChannelHandlerTest {
    private ChannelHandler beanHandler;
    private ChannelHandler stringHandler;

    @BeforeEach
    void createChannelHandler() {
        BeanMapper<TestBean> mapper = new BeanMappers().register(TestBean.class, new BeanClassNameValidator());
        beanHandler = new ChannelHandler(mapper, TestBean.class, mapper, null);
        stringHandler = new ChannelHandler(null, String.class, null, null);
    }

    @Test
    void toServerMessage() {
        assertThat(stringHandler.toServerMessage("message")).isEqualTo("message");
        assertThatThrownBy(() -> stringHandler.toServerMessage(new TestBean()))
            .isInstanceOf(Error.class)
            .hasMessageContaining("message class does not match");

        assertThat(beanHandler.toServerMessage(new TestBean())).isEqualTo("{}");
        assertThatThrownBy(() -> beanHandler.toServerMessage("message"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("message class does not match");
    }

    @Test
    void fromClientMessage() {
        assertThat(stringHandler.fromClientMessage("message")).isEqualTo("message");

        assertThatThrownBy(() -> beanHandler.fromClientMessage("message"))
            .isInstanceOf(BadRequestException.class);
    }

    public static class TestBean {
    }
}
