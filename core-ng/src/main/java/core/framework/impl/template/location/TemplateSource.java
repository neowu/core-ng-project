package core.framework.impl.template.location;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author neo
 */
public interface TemplateSource {
    BufferedReader reader() throws IOException;

    TemplateSource resolve(String path);

    String source();
}
