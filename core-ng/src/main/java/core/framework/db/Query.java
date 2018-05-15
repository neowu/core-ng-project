package core.framework.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Query<T> {
    void where(String condition, Object... params);

    void orderBy(String sort);

    void skip(int skip);

    void limit(int limit);

    List<T> fetch();

    Optional<T> fetchOne();

    <P> Optional<P> project(String projection, Class<P> viewClass);

    default int count() {
        return project("count(1)", Integer.class).orElseThrow(() -> new Error("unexpected result"));
    }
}
