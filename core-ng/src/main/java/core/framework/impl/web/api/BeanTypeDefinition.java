package core.framework.impl.web.api;

import core.framework.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public class BeanTypeDefinition {
    public final List<Field> fields = Lists.newArrayList();
    public String name;

    public static class Field {
        public String name;
        public String type;
        public boolean notNull;
    }
}
