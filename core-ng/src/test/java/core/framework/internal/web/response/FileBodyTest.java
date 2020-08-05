package core.framework.internal.web.response;

import core.framework.log.ErrorCode;
import core.framework.log.Severity;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class FileBodyTest {
    @Test
    void convertException() {
        var channel = mock(FileChannel.class);
        var callback = new FileBody.FileBodyCallback(channel);
        UncheckedIOException exception = callback.convertException(new ClosedChannelException());
        assertThat(exception)
                .isInstanceOf(FileBody.ClientAbortException.class)
                .isInstanceOf(ErrorCode.class)
                .satisfies(error -> assertThat(((ErrorCode) error).severity()).isEqualTo(Severity.WARN));
    }
}
