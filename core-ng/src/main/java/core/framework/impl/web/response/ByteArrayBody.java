package core.framework.impl.web.response;

/**
 * @author rainbow.cai
 */
public class ByteArrayBody implements Body {
    final byte[] bytes;

    public ByteArrayBody(byte[] bytes) {
        this.bytes = bytes;
    }
}
