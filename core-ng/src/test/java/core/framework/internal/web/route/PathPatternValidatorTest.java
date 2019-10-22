package core.framework.internal.web.route;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class PathPatternValidatorTest {
    @Test
    void validate() {
        new PathPatternValidator("/robot.txt", true).validate();
        new PathPatternValidator("/images", true).validate();

        new PathPatternValidator("/path-with-trailing-slash/", true).validate();

        new PathPatternValidator("/user/:id/name", true).validate();
        new PathPatternValidator("/v2/user/:id", true).validate();
        new PathPatternValidator("/ajax/:path(*)", true).validate();
    }

    @Test
    void duplicateVariable() {
        assertThatThrownBy(() -> new PathPatternValidator("/:name/path/:name", true).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void invalidVariable() {
        assertThatThrownBy(() -> new PathPatternValidator("/path/:name(", true).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining(":name(");
    }

    @Test
    void notAllowWildcardVariable() {
        assertThatThrownBy(() -> new PathPatternValidator("/path/:name(*)", false).validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("wildcard path variable is not allowed");
    }
}
