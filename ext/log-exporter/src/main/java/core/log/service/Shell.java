package core.log.service;

import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * @author neo
 */
public class Shell {
    private final Logger logger = LoggerFactory.getLogger(Shell.class);

    public void execute(String... commands) {
        var watch = new StopWatch();
        String command = String.join(" ", commands);
        try {
            logger.info("start process, command={}", command);
            Process process = new ProcessBuilder().command(commands).start();
            int status = process.waitFor();
            Result result = readOutput(process, status);    // read out after process ends, means process output can't be too large, otherwise it may block process
            if (!result.success()) {
                logger.error("process failed, error={}", result.error);
            }
        } catch (IOException | InterruptedException e) {
            throw new Error("failed to execute command, error=" + e.getMessage(), e);
        } finally {
            logger.info("process ended, elapsed={}, command={}", Duration.ofNanos(watch.elapsed()), command);
        }
    }

    private Result readOutput(Process process, int status) throws IOException {
        try (InputStream inputStream = process.getInputStream();
             InputStream error = process.getErrorStream()) {
            return new Result(status, new String(inputStream.readAllBytes(), StandardCharsets.UTF_8),
                new String(error.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    public record Result(int status, String output, String error) {
        public boolean success() {
            return status == 0;
        }
    }
}
