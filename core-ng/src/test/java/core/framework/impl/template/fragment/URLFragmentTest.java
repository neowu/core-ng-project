package core.framework.impl.template.fragment;

import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.TestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author neo
 */
class URLFragmentTest {
    private URLFragment fragment;

    @BeforeEach
    void createURLFragment() {
        fragment = new URLFragment("stringField", new TemplateMetaContext(TestModel.class), false, null);
    }

    @Test
    void isValidURL() {
        String protocolRelativeURL = "//localhost:8080/path1%20path2/path3?k1=v1%20v2&k2=v1+v2#f1/f2";
        assertThat(fragment.isValidURL(protocolRelativeURL)).isTrue();

        String httpsURL = "http://example.com/:@-._~!$&'()*+,=;:@-._~!$&'()*+,=:@-._~!$&'()*+,==?/?:@-._~!$'()*+,;=/?:@-._~!$'()*+,;==#/?:@-._~!$&'()*+,;=";
        assertThat(fragment.isValidURL(httpsURL)).isTrue();

        String dataImageURL = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==";
        assertThat(fragment.isValidURL(dataImageURL)).isTrue();

        assertFalse(fragment.isValidURL(null));
    }

    @Test
    void dataURL() {
        String dataImageURL = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==";
        assertThat(fragment.url(dataImageURL, new TemplateContext(new TestModel(), null)))
                .isEqualTo(dataImageURL);
    }
}
