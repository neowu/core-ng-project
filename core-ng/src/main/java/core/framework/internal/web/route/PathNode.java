package core.framework.internal.web.route;

import core.framework.internal.web.request.PathParams;
import core.framework.util.Strings;

import java.util.HashMap;
import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
class PathNode {
    private final String param;
    private URLHandler handler;
    private Map<String, PathNode> staticNodes;
    private PathNode dynamicNode;
    private PathNode wildcardNode;

    PathNode(String param) {
        this.param = param;
    }

    URLHandler register(String pathPattern) {
        return register(pathPattern, Path.parse(pathPattern).next);
    }

    private URLHandler register(String pathPattern, Path currentPath) {
        if (currentPath == null) {
            if (handler == null) handler = new URLHandler(pathPattern);
            return handler;
        } else if (Strings.startsWith(currentPath.value, ':')) {
            int paramIndex = currentPath.value.indexOf('(');
            int endIndex = paramIndex > 0 ? paramIndex : currentPath.value.length();
            String param = currentPath.value.substring(1, endIndex);
            boolean wildcard = paramIndex > 0;
            if (wildcard) {
                return registerWildcardNode(pathPattern, currentPath, param);
            } else {
                return registerDynamicNode(pathPattern, currentPath, param);
            }
        } else {
            if (staticNodes == null) staticNodes = new HashMap<>();
            PathNode staticNode = staticNodes.computeIfAbsent(currentPath.value, key -> new PathNode(null));
            return staticNode.register(pathPattern, currentPath.next);
        }
    }

    private URLHandler registerWildcardNode(String pathPattern, Path currentPath, String param) {
        if (currentPath.next != null) throw new Error(format("wildcard path variable must be the last, path={}, param={}", pathPattern, param));
        if (wildcardNode != null) {
            if (!Strings.equals(wildcardNode.param, param))
                throw new Error(format("found conflict dynamic pattern, path={}, param={}, conflictedParam={}", pathPattern, param, wildcardNode.param));
        } else {
            wildcardNode = new PathNode(param);
        }
        return wildcardNode.register(pathPattern, currentPath.next);
    }

    private URLHandler registerDynamicNode(String pathPattern, Path currentPath, String param) {
        if (dynamicNode != null) {
            if (!Strings.equals(dynamicNode.param, param))
                throw new Error(format("found conflict dynamic pattern, path={}, param={}, conflictedParam={}", pathPattern, param, dynamicNode.param));
        } else {
            dynamicNode = new PathNode(param);
        }
        return dynamicNode.register(pathPattern, currentPath.next);
    }

    URLHandler find(String path, PathParams pathParams) {
        return find(Path.parse(path), pathParams);
    }

    private URLHandler find(Path currentPath, PathParams pathParams) {
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
}
