package core.framework.impl.web.api;

import core.framework.util.Lists;
import core.framework.util.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class Namespace {
    public final Map<String, TypeDefinition> typeDefinitions = Maps.newLinkedHashMap();
    public final Map<String, EnumDefinition> enumDefinitions = Maps.newLinkedHashMap();
    public final List<ServiceDefinition> serviceDefinitions = Lists.newArrayList();
}
