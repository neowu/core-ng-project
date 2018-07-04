package core.framework.impl.web.response;

import core.framework.impl.validate.ValidationException;
import core.framework.impl.web.bean.BeanClassNameValidator;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.impl.web.bean.TestBean;
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
        Sender sender = mock(Sender.class);
        ResponseHandlerContext context = new ResponseHandlerContext(new ResponseBeanMapper(new BeanClassNameValidator()), null);
        BeanBody body = new BeanBody(new TestBean());
        assertThatThrownBy(() -> body.send(sender, context))
                .isInstanceOf(ValidationException.class);
    }
}
