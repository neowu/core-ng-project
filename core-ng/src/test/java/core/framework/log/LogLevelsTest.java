package core.framework.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogLevelsTest {
    @Test
    void add() {
        assertThatThrownBy(() -> {
            LogLevels.add("com.test.", LogLevel.WARN, LogLevel.INFO);
        }).hasMessageContaining("log with info level less than trace level may not be printed");
    }
}
