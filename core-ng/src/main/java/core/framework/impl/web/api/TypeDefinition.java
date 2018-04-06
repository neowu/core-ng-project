package core.framework.impl.web.api;

import core.framework.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public class TypeDefinition {
    public final List<FieldDefinition> fields = Lists.newArrayList();
    public String name;

    public static class FieldDefinition {
        public String name;
        public String type;
        public boolean notNull;
    }
}
