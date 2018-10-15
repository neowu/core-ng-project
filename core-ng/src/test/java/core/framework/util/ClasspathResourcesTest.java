package core.framework.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ClasspathResourcesTest {
    @Test
    void text() {
        String text = ClasspathResources.text("classpath-resource-test/resource.txt");
        assertThat(text).isEqualTo("value");
    }

    @Test
    void bytes() {
        assertThatThrownBy(() -> ClasspathResources.bytes("classpath-resource-test/not-existed-resource.properties"))
                .isInstanceOf(Error.class)
                .hasMessageContaining("can not load resource");
    }
}
