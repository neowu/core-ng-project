package core.framework.db;

import java.util.List;

/**
 * @author neo
 */
public interface Query<T> {
    Query<T> where(String condition, Object... params);

    Query<T> orderBy(String sort);

    Query<T> skip(int skip);

    Query<T> limit(int limit);

    List<T> fetch();

    int count();
}
