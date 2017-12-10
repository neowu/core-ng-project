package core.framework.impl.web.api;

import core.framework.util.Lists;
import core.framework.util.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
class JSONNode {
    Map<String, Object> properties;

    JSONNode() {
        properties = Maps.newLinkedHashMap();
    }

    private JSONNode(Map<String, Object> properties) {
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    JSONNode get(String key) {
        return new JSONNode((Map<String, Object>) this.properties.computeIfAbsent(key, k -> Maps.newLinkedHashMap()));
    }

    boolean has(String key) {
        return properties.containsKey(key);
    }

    void put(String key, Object value) {
        if (value instanceof JSONNode) {
            properties.put(key, ((JSONNode) value).properties);
        } else {
            properties.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    void add(String key, Object value) {
        List<Object> list = (List<Object>) this.properties.computeIfAbsent(key, k -> Lists.newArrayList());
        if (value instanceof JSONNode) {
            list.add(((JSONNode) value).properties);
        } else {
            list.add(value);
        }
    }
}
