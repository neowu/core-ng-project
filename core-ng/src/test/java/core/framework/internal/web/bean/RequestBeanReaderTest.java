package core.framework.internal.web.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RequestBeanReaderTest {
    private RequestBeanReader reader;

    @BeforeEach
    void createRequestBeanReader() {
        reader = new RequestBeanReader();
    }

    @Test
    void fromJSON() {
        assertThatThrownBy(() -> reader.fromJSON(String.class, new byte[0]))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class must not be java built-in class");
    }

}
