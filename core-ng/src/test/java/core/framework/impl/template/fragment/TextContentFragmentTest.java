package core.framework.impl.template.fragment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class TextContentFragmentTest {
    @Test
    void escapeHTML() {
        assertEquals("text", TextContentFragment.escapeHTML("text"));
        assertEquals("before&lt;p&gt;text&lt;&#47;p&gt;after", TextContentFragment.escapeHTML("before<p>text</p>after"));
        assertEquals("&lt;html&gt;&lt;&#47;html&gt;", TextContentFragment.escapeHTML("<html></html>"));
    }
}
