package core.log.service;

import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class Shell {
    private final Logger logger = LoggerFactory.getLogger(Shell.class);

    public void execute(String... commands) {
        var watch = new StopWatch();
        String command = String.join(" ", commands);
        try {
            Process process = new ProcessBuilder().command(commands).start();
            int status = process.waitFor();
            Result result = readOutput(process, status);    // read out after process ends, means process output can't be too large, otherwise it may block process
            if (!result.success()) {
                throw new Error(format("failed to execute command, command={}, error={}", command, result.error));
            }
        } catch (IOException | InterruptedException e) {
            throw new Error(format("failed to execute command, command={}, error={}", command, e.getMessage()), e);
        } finally {
            logger.info("execute command, command={}, elapsed={}", command, Duration.ofNanos(watch.elapsed()));
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
