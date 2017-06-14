package core.framework.impl.web.route;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author neo
 */
public class PathPatternValidatorTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void duplicateVariable() {
        exception.expect(Error.class);
        exception.expectMessage("duplicate");

        new PathPatternValidator("/:name/path/:name").validate();
    }

    @Test
    public void validate() {
        new PathPatternValidator("/robot.txt").validate();
        new PathPatternValidator("/images").validate();

        new PathPatternValidator("/path-with-trailing-slash/").validate();

        new PathPatternValidator("/user/:id/name").validate();
        new PathPatternValidator("/v2/user/:id").validate();
    }

    @Test
    public void invalidVariable() {
        exception.expect(Error.class);
        exception.expectMessage(":name(");

        new PathPatternValidator("/path/:name(").validate();
    }
}
