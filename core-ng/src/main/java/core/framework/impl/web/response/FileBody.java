package core.framework.impl.web.response;

import java.io.File;

/**
 * @author neo
 */
public class FileBody implements Body {
    final File file;

    public FileBody(File file) {
        this.file = file;
    }
}
