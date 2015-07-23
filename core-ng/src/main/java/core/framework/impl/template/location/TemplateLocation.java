package core.framework.impl.template.location;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author neo
 */
public interface TemplateLocation {
    BufferedReader reader() throws IOException;

    TemplateLocation location(String path);
}
