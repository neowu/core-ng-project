package core.framework.internal.web.websocket;

import core.framework.internal.web.bean.BeanMappers;
import core.framework.internal.web.bean.ResponseBeanMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ChannelImplTest {
    @Test
    void context() {
        var channel = new ChannelImpl(null, null, null, null);
        channel.context().put("k1", "v1");
        assertThat(channel.context().get("k1")).isEqualTo("v1");

        channel.context().put("k1", null);
        assertThat(channel.context().get("k1")).isNull();
    }

    @Test
    void send() {
        var channel = new ChannelImpl(null, null, null, new ResponseBeanMapper(new BeanMappers()));

        assertThatThrownBy(() -> channel.send(new TestBean()))
            .isInstanceOf(Error.class)
            .hasMessageContaining("bean class is not registered");
    }

    public static class TestBean {

    }
}
