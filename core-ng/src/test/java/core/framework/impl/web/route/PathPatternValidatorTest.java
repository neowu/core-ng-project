package core.framework.impl.web.route;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class PathPatternValidatorTest {
    @Test
    void validate() {
        new PathPatternValidator("/robot.txt").validate();
        new PathPatternValidator("/images").validate();

        new PathPatternValidator("/path-with-trailing-slash/").validate();

        new PathPatternValidator("/user/:id/name").validate();
        new PathPatternValidator("/v2/user/:id").validate();
        new PathPatternValidator("/ajax/:path(*)").validate();
    }

    @Test
    void duplicateVariable() {
        assertThatThrownBy(() -> new PathPatternValidator("/:name/path/:name").validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void invalidVariable() {
        assertThatThrownBy(() -> new PathPatternValidator("/path/:name(").validate())
                .isInstanceOf(Error.class)
                .hasMessageContaining(":name(");
    }
}
