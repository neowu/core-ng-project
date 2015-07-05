package core.framework.api.db;


/**
 * @author neo
 */
public interface Transaction extends AutoCloseable {
    void rollback();

    void commit();

    @Override
    void close();
}
