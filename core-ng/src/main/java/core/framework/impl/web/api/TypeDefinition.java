package core.framework.impl.web.api;

import core.framework.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public class TypeDefinition {
    public String name;
    public List<FieldDefinition> fields = Lists.newArrayList();

    public static class FieldDefinition {
        public String name;
        public String type;
        public boolean notNull;
    }
}
