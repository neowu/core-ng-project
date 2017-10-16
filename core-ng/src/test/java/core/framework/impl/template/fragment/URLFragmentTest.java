package core.framework.impl.template.fragment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class URLFragmentTest {
    @Test
    void isValidURL() {
        assertTrue(URLFragment.isValidURL("//localhost:8080/path1%20path2/path3?k1=v1%20v2&k2=v1+v2#f1/f2"));
        assertTrue(URLFragment.isValidURL("http://example.com/:@-._~!$&'()*+,=;:@-._~!$&'()*+,=:@-._~!$&'()*+,==?/?:@-._~!$'()*+,;=/?:@-._~!$'()*+,;==#/?:@-._~!$&'()*+,;="));

        assertFalse(URLFragment.isValidURL(null));
    }
}
