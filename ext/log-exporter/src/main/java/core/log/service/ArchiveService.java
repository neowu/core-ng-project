package core.log.service;

import core.framework.crypto.Hash;
import core.framework.inject.Inject;
import core.framework.util.Network;
import core.framework.util.Strings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * @author neo
 */
public class ArchiveService {
    private final String hash = Hash.md5Hex(Network.LOCAL_HOST_NAME).substring(0, 5);   // generally there only need one log-exporter, this is to avoid file name collision with multiple log-exporter
    private final Shell shell = new Shell();

    Path logDir = Path.of("/var/log/app");

    @Inject
    UploadService uploadService;

    public void uploadArchive(LocalDate date) {
        String actionLogPath = actionLogPath(date);
        Path actionLogFilePath = Path.of(logDir.toString(), actionLogPath);
        if (Files.exists(actionLogFilePath)) {
            uploadService.uploadAsync(actionLogFilePath, actionLogPath);
        }

        Path traceLogDirPath = Path.of(logDir.toString(), Strings.format("/trace/{}", date));
        if (Files.exists(traceLogDirPath)) {
            uploadService.uploadDirAsync(traceLogDirPath, "/trace");
        }
    }

    public void cleanupArchive(LocalDate date) {
        Path actionLogFilePath = Path.of(logDir.toString(), actionLogPath(date));
        shell.execute("rm", "-f", actionLogFilePath.toString());

        Path traceLogDirPath = Path.of(logDir.toString(), Strings.format("/trace/{}", date));
        shell.execute("rm", "-rf", traceLogDirPath.toString());
    }

    public String actionLogPath(LocalDate date) {
        return Strings.format("/action/{}/action-{}-{}.ndjson", date.getYear(), date, hash);
    }

    public String traceLogPath(LocalDate date, String app, String id) {
        return Strings.format("/trace/{}/{}/{}.txt", date, app, id);
    }
}
