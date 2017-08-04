package core.framework.impl.web.response;

import java.nio.file.Path;

/**
 * @author neo
 */
public class FileBody implements Body {
    final Path path;

    public FileBody(Path path) {
        this.path = path;
    }
}
