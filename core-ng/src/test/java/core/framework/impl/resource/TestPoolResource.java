package core.framework.impl.resource;

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
