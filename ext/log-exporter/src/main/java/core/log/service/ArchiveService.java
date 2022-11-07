package core.log.service;

import core.framework.crypto.Hash;
import core.framework.inject.Inject;
import core.framework.util.Network;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * @author neo
 */
public class ArchiveService {
    private final Logger logger = LoggerFactory.getLogger(ArchiveService.class);
    private final String hash = Hash.md5Hex(Network.LOCAL_HOST_NAME).substring(0, 5);   // generally there only need one log-exporter, this is to avoid file name collision with multiple log-exporter
    private final Shell shell = new Shell();

    Path logDir = Path.of("/var/log/app");

    @Inject
    UploadService uploadService;

    public void uploadActionLog(LocalDate date) {
        String actionLogPath = actionLogPath(date);
        Path actionLogFilePath = Path.of(logDir.toString(), actionLogPath);
        if (Files.exists(actionLogFilePath)) {
            uploadService.uploadAsync(actionLogFilePath, actionLogPath);
        }
    }

    public void deleteArchive(LocalDate date) {
        Path actionLogFilePath = Path.of(logDir.toString(), actionLogPath(date));
        logger.info("cleanup action log, path={}", actionLogFilePath);
        shell.execute("rm", "-f", actionLogFilePath.toString());

        Path traceLogDirPath = Path.of(logDir.toString(), Strings.format("/trace/{}", date));
        logger.info("cleanup trace log, path={}", traceLogDirPath);
        shell.execute("rm", "-rf", traceLogDirPath.toString());
    }

    public String actionLogPath(LocalDate date) {
        return Strings.format("/action/{}/action-{}-{}.ndjson", date.getYear(), date, hash);
    }

    public String traceLogPath(LocalDate date, String app, String id) {
        return Strings.format("/trace/{}/{}/{}.txt", date, app, id);
    }
}
