package core.log.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ShellTest {
    private Shell shell;

    @BeforeEach
    void createShell() {
        shell = new Shell();
    }

    @Test
    void executeInvalidCommand() {
        assertThatThrownBy(() -> shell.execute("invalid_command"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("failed to execute command");
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void execute() {
        shell.execute("ls");

        assertThatThrownBy(() -> shell.execute("ls", "/invalid"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("failed to execute command");
    }
}
