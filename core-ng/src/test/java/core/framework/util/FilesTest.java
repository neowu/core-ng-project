package core.framework.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class FilesTest {
    @Test
    void tempFile() {
        Path file = Files.tempFile();
        assertThat(file.getFileName().toString()).endsWith(".tmp");
    }

    @Test
    void tempDir() {
        Path tempDir = Files.tempDir();
        assertThat(tempDir).exists().isDirectory();

        Files.deleteDir(tempDir);
    }
}
