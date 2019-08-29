package core.framework.internal.resource;

/**
 * @author neo
 */
class TestPoolResource implements AutoCloseable {
    boolean closed;

    @Override
    public void close() {
        closed = true;
    }
}
