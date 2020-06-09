package core.framework.internal.web.websocket;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.web.bean.BeanMapper;
import core.framework.internal.web.bean.BeanMappers;
import core.framework.web.exception.BadRequestException;
import core.framework.web.websocket.Channel;
import core.framework.web.websocket.ChannelListener;
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
        beanHandler = new ChannelHandler(mapper, TestBean.class, mapper, new TestBeanListener());
        stringHandler = new ChannelHandler(null, String.class, null, new TestStringListener());
    }

    @Test
    void toServerMessage() {
        var bean = new TestBean();
        bean.value = "value";
        assertThat(stringHandler.toServerMessage("message")).isEqualTo("message");
        assertThatThrownBy(() -> stringHandler.toServerMessage(bean))
                .isInstanceOf(Error.class)
                .hasMessageContaining("message class does not match");

        assertThat(beanHandler.toServerMessage(bean)).isEqualTo("{\"value\":\"value\"}");
        assertThatThrownBy(() -> beanHandler.toServerMessage("message"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("message class does not match");
    }

    @Test
    void fromClientMessage() {
        assertThat(stringHandler.fromClientMessage("message")).isEqualTo("message");

        assertThatThrownBy(() -> beanHandler.fromClientMessage("message"))
                .isInstanceOf(BadRequestException.class)
                .satisfies(e -> assertThat(((BadRequestException) e).errorCode()).isEqualTo("INVALID_WS_MESSAGE"));

        assertThatThrownBy(() -> beanHandler.fromClientMessage("{}"))
                .isInstanceOf(BadRequestException.class)
                .satisfies(e -> assertThat(((BadRequestException) e).errorCode()).isEqualTo("VALIDATION_ERROR"));
    }

    public static class TestBean {
        @NotNull
        @Property(name = "value")
        public String value;
    }

    static class TestBeanListener implements ChannelListener<TestBean> {
        @Override
        public void onMessage(Channel channel, TestBean message) {
        }
    }

    static class TestStringListener implements ChannelListener<String> {
        @Override
        public void onMessage(Channel channel, String message) {
        }
    }
}
