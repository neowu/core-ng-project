package core.framework.impl.web.route;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.impl.web.request.PathParams;

import java.util.Map;

/**
 * @author neo
 */
class PathNode {
    protected URLHandler handler;
    private Map<String, PathNode> staticNodes;
    private DynamicNode dynamicNode;
    private DynamicNode wildcardNode;

    URLHandler register(String pathPattern) {
        return register(pathPattern, Path.parse(pathPattern).next);
    }

    protected URLHandler register(String pathPattern, Path currentPath) {
        if (currentPath == null) {
            if (handler == null) handler = new URLHandler(pathPattern);
            return handler;
        } else if (currentPath.value.startsWith(":")) {
            int paramIndex = currentPath.value.indexOf('(');
            int endIndex = paramIndex > 0 ? paramIndex : currentPath.value.length();
            String name = currentPath.value.substring(1, endIndex);
            boolean wildcard = paramIndex > 0;
            if (wildcard) {
                return registerWildcardNode(pathPattern, currentPath, name);
            } else {
                return registerDynamicNode(pathPattern, currentPath, name);
            }
        } else {
            if (staticNodes == null) staticNodes = Maps.newHashMap();
            PathNode staticNode = staticNodes.computeIfAbsent(currentPath.value, key -> new PathNode());
            return staticNode.register(pathPattern, currentPath.next);
        }
    }

    private URLHandler registerWildcardNode(String pathPattern, Path currentPath, String name) {
        if (currentPath.next != null) throw Exceptions.error("wildcard must be at end of path pattern, path={}", pathPattern);
        if (wildcardNode != null) {
            if (!Strings.equals(wildcardNode.param, name))
                throw Exceptions.error("found conflict dynamic pattern, path={}, param={}, conflictedParam={}", pathPattern, name, wildcardNode.param);
        } else {
            wildcardNode = new DynamicNode(name);
        }
        return wildcardNode.register(pathPattern, currentPath.next);
    }

    private URLHandler registerDynamicNode(String pathPattern, Path currentPath, String name) {
        if (dynamicNode != null) {
            if (!Strings.equals(dynamicNode.param, name))
                throw Exceptions.error("found conflict dynamic pattern, path={}, param={}, conflictedParam={}", pathPattern, name, dynamicNode.param);
        } else {
            dynamicNode = new DynamicNode(name);
        }
        return dynamicNode.register(pathPattern, currentPath.next);
    }

    URLHandler find(String path, PathParams pathParams) {
        return find(Path.parse(path), pathParams);
    }

    protected URLHandler find(Path currentPath, PathParams pathParams) {
        Path nextPath = currentPath.next;
        if (nextPath == null) return handler;

        URLHandler handler = findStatic(nextPath, pathParams);
        if (handler != null) return handler;

        if (!"/".equals(nextPath.value)) {  // dynamic node should not match trailing slash
            handler = findDynamic(nextPath, pathParams);
            if (handler != null) return handler;
        }

        if (wildcardNode != null) {
            pathParams.put(wildcardNode.param, nextPath.subPath());
            return wildcardNode.handler;
        }

        return null;
    }

    private URLHandler findStatic(Path nextPath, PathParams pathParams) {
        if (staticNodes != null) {
            PathNode nextNode = staticNodes.get(nextPath.value);
            if (nextNode != null) {
                return nextNode.find(nextPath, pathParams);
            }
        }
        return null;
    }

    private URLHandler findDynamic(Path nextPath, PathParams pathParams) {
        if (dynamicNode != null) {
            URLHandler handler = dynamicNode.find(nextPath, pathParams);
            if (handler != null) {
                pathParams.put(dynamicNode.param, nextPath.value);
                return handler;
            }
        }
        return null;
    }

    static class DynamicNode extends PathNode {
        final String param;

        DynamicNode(String param) {
            this.param = param;
        }
    }
}
