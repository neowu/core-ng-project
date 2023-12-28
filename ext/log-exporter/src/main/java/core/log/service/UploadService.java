package core.log.service;

import java.nio.file.Path;

/**
 * @author neo
 */
public class UploadService {    // support AWS/Azure in future if needed
    private final Shell shell = new Shell();
    private final String bucket;

    public UploadService(String bucket) {
        this.bucket = bucket;
    }

    public void upload(Path file, String remotePath) {
        // requires '-q', otherwise standard output may block if buffer is full, Shell reads std after process ends
        // and '-m' may stress network bandwidth, currently not really necessary
        shell.execute("gsutil", "-q", "cp", file.toString(), bucket + remotePath);
    }
}
