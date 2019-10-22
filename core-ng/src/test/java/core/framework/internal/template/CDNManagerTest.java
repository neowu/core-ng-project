package core.framework.internal.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CDNManagerTest {
    private CDNManager manager;

    @BeforeEach
    void createCDNManager() {
        manager = new CDNManager();
        manager.host("cdn");
    }

    @Test
    void url() {
        assertThat(manager.url("/image/image2.png")).isEqualTo("//cdn/image/image2.png");
        assertThat(manager.url("/image/image3.png")).isEqualTo("//cdn/image/image3.png");
        assertThat(manager.url("/image/image3.png?param=value")).isEqualTo("//cdn/image/image3.png?param=value");
    }

    @Test
    void absoluteURL() {
        String absoluteURL = "//host2/image/image1.png";
        assertThat(manager.url(absoluteURL)).isEqualTo(absoluteURL);
    }

    @Test
    void dataURL() {
        String dataImageURL = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==";
        assertThat(manager.url(dataImageURL)).isEqualTo(dataImageURL);
    }
}
