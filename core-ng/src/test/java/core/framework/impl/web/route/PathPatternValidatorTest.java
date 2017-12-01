package core.framework.impl.web.route;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class PathPatternValidatorTest {
    @Test
    void duplicateVariable() {
        Error error = assertThrows(Error.class, () -> new PathPatternValidator("/:name/path/:name").validate());
        assertThat(error.getMessage()).contains("duplicate");
    }

    @Test
    void validate() {
        new PathPatternValidator("/robot.txt").validate();
        new PathPatternValidator("/images").validate();

        new PathPatternValidator("/path-with-trailing-slash/").validate();

        new PathPatternValidator("/user/:id/name").validate();
        new PathPatternValidator("/v2/user/:id").validate();
    }

    @Test
    void invalidVariable() {
        Error error = assertThrows(Error.class, () -> new PathPatternValidator("/path/:name(").validate());
        assertThat(error.getMessage()).contains(":name(");
    }
}
