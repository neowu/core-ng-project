package core.framework.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * @author neo
 */
public final class InputStreams {
    public static byte[] bytes(InputStream stream) {
        final int bufferSize = 0x1000; // 4K

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[bufferSize];
        int len;
        try {
            while (true) {
                len = stream.read(buf);
                if (len < 0) break;
                output.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return output.toByteArray();
    }
}
