package core.log.service;

import core.framework.crypto.Hash;
import core.framework.inject.Inject;
import core.framework.util.Network;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

/**
 * @author neo
 */
public class ArchiveService {
    private final Logger logger = LoggerFactory.getLogger(ArchiveService.class);
    private final String hash = Hash.md5Hex(Network.LOCAL_HOST_NAME).substring(0, 5);   // generally there only need one log-exporter, this is to avoid file name collision with multiple log-exporter
    private final Shell shell = new Shell();

    public Path logDir = Path.of("/var/log/app");

    @Inject
    UploadService uploadService;

    public void uploadArchive(LocalDate date) {
        logger.info("uploading begin, date={}", date);

        String actionLogPath = actionLogPath(date);
        Path actionLogFilePath = Path.of(logDir.toString(), actionLogPath);
        if (exists(actionLogFilePath)) {
            uploadService.upload(actionLogFilePath, actionLogPath);
        }

        String eventPath = eventPath(date);
        Path eventFilePath = Path.of(logDir.toString(), eventPath);
        if (exists(eventFilePath)) {
            uploadService.upload(eventFilePath, eventPath);
        }

        logger.info("uploading end, date={}", date);
    }

    public void cleanupArchive(LocalDate date) {
        logger.info("cleaning up archives, date={}", date);

        Path actionLogFilePath = Path.of(logDir.toString(), actionLogPath(date));
        shell.execute("rm", "-f", actionLogFilePath.toString());

        Path eventFilePath = Path.of(logDir.toString(), eventPath(date));
        shell.execute("rm", "-f", eventFilePath.toString());
    }

    public String actionLogPath(LocalDate date) {
        return Strings.format("/action/{}/action-{}-{}.ndjson", date.getYear(), date, hash);
    }

    public String eventPath(LocalDate date) {
        return Strings.format("/event/{}/event-{}-{}.ndjson", date.getYear(), date, hash);
    }

    public Path initializeLogFilePath(String logPath) throws IOException {
        Path path = Path.of(logDir.toString(), logPath);
        Path parent = path.getParent();
        if (parent != null && !exists(parent)) createDirectories(parent);
        return path;
    }
}
