package core.log.service;

import core.framework.crypto.Hash;
import core.framework.inject.Inject;
import core.framework.util.Files;
import core.framework.util.Network;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

        Path traceLogDirPath = Path.of(logDir.toString(), Strings.format("/trace/{}", date));
        if (exists(traceLogDirPath)) {
            File[] appDirs = traceLogDirPath.toFile().listFiles(File::isDirectory);
            if (appDirs != null) {
                for (File appDir : appDirs) {
                    String app = appDir.getName();
                    String traceLogPath = Strings.format("/trace/{}/{}-{}-{}.tar.gz", date, app, date, hash);
                    Path traceLogFilePath = Path.of(logDir.toString(), traceLogPath);
                    shell.execute("tar", "-czf", traceLogFilePath.toString(), "-C", logDir.toString(), Strings.format("trace/{}/{}", date, app));
                    uploadService.upload(traceLogFilePath, traceLogPath);
                }
            }
        }

        logger.info("uploading end, date={}", date);
    }

    public void cleanupArchive(LocalDate date) {
        logger.info("cleaning up archives, date={}", date);

        Path actionLogFilePath = Path.of(logDir.toString(), actionLogPath(date));
        shell.execute("rm", "-f", actionLogFilePath.toString());

        Path eventFilePath = Path.of(logDir.toString(), eventPath(date));
        shell.execute("rm", "-f", eventFilePath.toString());

        Path traceLogDirPath = Path.of(logDir.toString(), Strings.format("/trace/{}", date));
        if (exists(traceLogDirPath)) {
            logger.info("delete trace logs, path={}", traceLogDirPath);
            // use shell (rm -rf or find) may cause pod terminate with error code 137 on mounted disk
            Files.deleteDir(traceLogDirPath);
        }
    }

    public String actionLogPath(LocalDate date) {
        return Strings.format("/action/{}/action-{}-{}.ndjson", date.getYear(), date, hash);
    }

    public String eventPath(LocalDate date) {
        return Strings.format("/event/{}/event-{}-{}.ndjson", date.getYear(), date, hash);
    }

    public String traceLogPath(LocalDateTime now, String app, String id) {
        return Strings.format("/trace/{}/{}/{}/{}.txt", now.toLocalDate(), app, now.getHour(), id);
    }

    public Path initializeLogFilePath(String logPath) throws IOException {
        Path path = Path.of(logDir.toString(), logPath);
        Path parent = path.getParent();
        if (parent != null && !exists(parent)) createDirectories(parent);
        return path;
    }
}
