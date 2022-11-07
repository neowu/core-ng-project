package core.log.service;

import core.framework.async.Executor;
import core.framework.inject.Inject;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author neo
 */
public class UploadService {
    private final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private final Shell shell = new Shell();
    private final String bucket;
    @Inject
    Executor executor;

    public UploadService(String bucket) {
        this.bucket = bucket;
    }

    public void uploadAsync(Path file, String remotePath) {
        executor.submit("upload", () -> {
            String uri = Strings.format("{}/{}", bucket, remotePath);
            logger.info("upload, file={}, url={}", file, uri);
            // requires '-q', otherwise standard output may block if buffer is full, Shell reads std after process ends
            shell.execute("gsutil", "-m", "-q", "cp", file.toString(), uri);
        });
    }
}
