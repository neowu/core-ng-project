package core.framework.db;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface Query<T> {
    void where(String condition, Object... params);

    void orderBy(String sort);

    void groupBy(String groupBy);

    void skip(Integer skip);    // pass null to reset skip

    void limit(Integer limit);  // pass null to reset limit

    List<T> fetch();

    Optional<T> fetchOne();

    <P> List<P> project(String projection, Class<P> viewClass);

    <P> Optional<P> projectOne(String projection, Class<P> viewClass);

    // refer to https://dev.mysql.com/doc/refman/8.0/en/group-by-functions.html#function_count, count function return BIGINT
    default long count() {
        return projectOne("count(1)", Long.class).orElseThrow();
    }

    // syntax sugar, to help to build "where in clause" with dynamic params
    default <V> void in(String field, List<V> params) {
        if (field == null) throw new Error("field must not be null");
        if (params == null || params.isEmpty()) throw new Error("params must not be empty");
        // efficient version of: where(Strings.format("{} IN ({})", field, params.stream().map(param -> "?").collect(Collectors.joining(", "))), params.toArray());
        int size = params.size();
        @SuppressWarnings("PMD.InsufficientStringBufferDeclaration")    // false positive
        var builder = new StringBuilder(field.length() + size * 3 + 6);    // e.g. field in (?, ?, ?)
        builder.append(field).append(" IN (");
        for (int i = 0; i < size; i++) {
            if (i > 0) builder.append(", ");
            builder.append('?');
        }
        builder.append(')');
        where(builder.toString(), params.toArray());
    }
}
