package core.framework.api.web;

import java.io.File;

/**
 * @author neo
 */
public class MultipartFile {
    public final File file;
    public final String fileName;
    public final String contentType;

    public MultipartFile(File file, String fileName, String contentType) {
        this.file = file;
        this.fileName = fileName;
        this.contentType = contentType;
    }
}
