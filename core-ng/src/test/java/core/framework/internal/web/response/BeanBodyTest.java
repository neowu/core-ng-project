package core.framework.internal.web.response;

import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.bean.TestBean;
import core.framework.internal.validate.ValidationException;
import core.framework.internal.web.bean.ResponseBeanWriter;
import io.undertow.io.Sender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class BeanBodyTest {
    @Mock
    Sender sender;

    @Test
    void send() {
        var writer = new ResponseBeanWriter();
        writer.register(TestBean.class, new BeanClassValidator());
        var context = new ResponseHandlerContext(writer, null);
        var body = new BeanBody(new TestBean());
        assertThatThrownBy(() -> body.send(sender, context))
                .isInstanceOf(ValidationException.class);
    }
}
