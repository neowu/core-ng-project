package core.framework.impl.template.fragment;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class TextContentFragmentTest {
    @Test
    public void escapeHTML() {
        Assert.assertEquals("text", TextContentFragment.escapeHTML("text"));
        Assert.assertEquals("&lt;html&gt;&lt;&#47;html&gt;", TextContentFragment.escapeHTML("<html></html>"));
    }
}