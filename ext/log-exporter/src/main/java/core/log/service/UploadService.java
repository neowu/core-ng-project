package core.log.service;

import core.framework.async.Executor;
import core.framework.inject.Inject;

import java.nio.file.Path;

/**
 * @author neo
 */
public class UploadService {
    private final Shell shell = new Shell();
    private final String bucket;
    @Inject
    Executor executor;

    public UploadService(String bucket) {
        this.bucket = bucket;
    }

    public void uploadAsync(Path file, String remotePath) {
        executor.submit("upload", () -> {
            // requires '-q', otherwise standard output may block if buffer is full, Shell reads std after process ends
            shell.execute("gsutil", "-q", "cp", file.toString(), bucket + remotePath);
        });
    }

    public void uploadDirAsync(Path dir, String remotePath) {
        executor.submit("upload", () -> {
            // requires '-q', otherwise standard output may block if buffer is full, Shell reads std after process ends
            shell.execute("gsutil", "-mq", "-o", "Boto:parallel_process_count=2", "-o", "Boto:parallel_thread_count=2", "cp", "-r", dir.toString(), bucket + remotePath);
        });
    }
}
