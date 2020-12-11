package core.framework.web.site;

import core.framework.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class WebDirectoryTest {
    @BeforeEach
    void cleanup() {
        System.clearProperty("core.webPath");
    }

    @Test
    void locateRootDirectory() {
        Path tempDir = Files.tempDir();
        System.setProperty("core.webPath", tempDir.toString());
        var directory = new WebDirectory();
        assertThat(directory.root()).isEqualTo(tempDir);
        Files.deleteDir(tempDir);
    }

    @Test
    void failedToLocateRootDir() {
        var directory = new WebDirectory();
        assertThatThrownBy(directory::root)
                .isInstanceOf(Error.class)
                .hasMessageContaining("can not find web path");
    }
}
