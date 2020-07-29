package core.framework.internal.web.response;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.bean.TestBean;
import core.framework.internal.validate.ValidationException;
import core.framework.internal.web.bean.ResponseBeanWriter;
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
        var writer = new ResponseBeanWriter();
        writer.register(TestBean.class, new BeanClassValidator());
        var context = new ResponseHandlerContext(writer, null);
        var body = new BeanBody(new TestBean());
        assertThatThrownBy(() -> body.send(sender, context))
                .isInstanceOf(ValidationException.class);
    }
}
