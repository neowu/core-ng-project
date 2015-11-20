package core.framework.impl.template.fragment;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class URLFragmentTest {
    @Test
    public void isValidURL() {
        assertTrue(URLFragment.isValidURL("http://example.com/:@-._~!$&'()*+,=;:@-._~!$&'()*+,=:@-._~!$&'()*+,==?/?:@-._~!$'()*+,;=/?:@-._~!$'()*+,;==#/?:@-._~!$&'()*+,;="));
    }
}