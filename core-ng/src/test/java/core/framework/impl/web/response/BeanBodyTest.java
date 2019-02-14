package core.framework.impl.web.response;

import core.framework.impl.web.bean.BeanBodyMapperRegistry;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.impl.web.bean.TestBean;
import core.framework.internal.validate.ValidationException;
import io.undertow.io.Sender;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class BeanBodyTest {
    @Test
    void send() {
        var sender = mock(Sender.class);
        var context = new ResponseHandlerContext(new ResponseBeanMapper(new BeanBodyMapperRegistry()), null);
        var body = new BeanBody(new TestBean());
        assertThatThrownBy(() -> body.send(sender, context))
                .isInstanceOf(ValidationException.class);
    }
}
