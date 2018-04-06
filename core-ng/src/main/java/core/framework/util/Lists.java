package core.framework.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author neo
 */
public final class Lists {
    public static <T> List<T> newArrayList() {
        return new ArrayList<>();
    }

    public static <T> List<T> newArrayList(T element) {
        List<T> list = new ArrayList<>(1);
        list.add(element);
        return list;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> newArrayList(T... elements) {
        List<T> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }
}
