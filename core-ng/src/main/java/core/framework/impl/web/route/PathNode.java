package core.framework.impl.web.route;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.impl.web.request.PathParams;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
class PathNode {
    private static final Pattern DYNAMIC_PATH_PATTERN = Pattern.compile("\\:(\\w+)(\\(([^\\(\\)]*)\\)){0,1}");

    private final Map<String, PathNode> staticNodes = Maps.newHashMap();
    private final List<DynamicNode> dynamicNodes = Lists.newArrayList();
    protected URLHandler handler;
    private DynamicNode wildcardNode;

    URLHandler register(String pathPattern) {
        return register(pathPattern, Path.parse(pathPattern).next);
    }

    protected URLHandler register(String pathPattern, Path currentPath) {
        if (currentPath == null) {
            if (handler == null) handler = new URLHandler(pathPattern);
            return handler;
        } else if (currentPath.value.startsWith(":")) {
            Matcher matcher = DYNAMIC_PATH_PATTERN.matcher(currentPath.value);
            if (!matcher.matches())
                throw Exceptions.error("path param must follow :name or :name(regex|*), path={}", pathPattern);
            String name = matcher.group(1);
            String pattern = matcher.group(3);
            return registerDynamicNode(pathPattern, currentPath, name, pattern);
        } else {
            PathNode staticNode = staticNodes.get(currentPath.value);
            if (staticNode == null) {
                staticNode = new PathNode();
                staticNodes.put(currentPath.value, staticNode);
            }
            return staticNode.register(pathPattern, currentPath.next);
        }
    }

    private URLHandler registerDynamicNode(String pathPattern, Path currentPath, String name, String pattern) {
        if ("*".equals(pattern)) {
            if (currentPath.next != null)
                throw Exceptions.error("wildcard must at end of path pattern, path={}", pathPattern);
            if (wildcardNode != null && !Strings.equals(wildcardNode.param, name))
                throw Exceptions.error("conflict dynamic pattern found, path={}, param={}, conflictedParam={}",
                    pathPattern, name, wildcardNode.param);

            wildcardNode = new DynamicNode(name, null);
            return wildcardNode.register(pathPattern, currentPath.next);
        } else {
            DynamicNode dynamicNode = getOrCreateDynamicNode(pathPattern, name, pattern);
            return dynamicNode.register(pathPattern, currentPath.next);
        }
    }

    DynamicNode getOrCreateDynamicNode(String pathPattern, String paramName, String paramPattern) {
        for (DynamicNode dynamicNode : dynamicNodes) {
            if ((paramPattern == null && dynamicNode.pattern == null)
                || (paramPattern != null && paramPattern.equals(dynamicNode.pattern.pattern()))) {
                if (!Strings.equals(dynamicNode.param, paramName))
                    throw Exceptions.error("conflict dynamic pattern found, path={}, param={}, conflictedParam={}",
                        pathPattern, paramName, dynamicNode.param);
                return dynamicNode;
            }
        }

        DynamicNode node = new DynamicNode(paramName, paramPattern);
        dynamicNodes.add(node);
        return node;
    }

    URLHandler find(String path, PathParams pathParams) {
        return find(Path.parse(path), pathParams);
    }

    protected URLHandler find(Path currentPath, PathParams pathParams) {
        Path nextPath = currentPath.next;
        if (nextPath == null) return handler;

        PathNode nextNode = staticNodes.get(nextPath.value);
        if (nextNode != null) {
            URLHandler handler = nextNode.find(nextPath, pathParams);
            if (handler != null) return handler;
        }

        for (DynamicNode dynamicNode : dynamicNodes) {
            if (dynamicNode.match(nextPath.value)) {
                URLHandler handler = dynamicNode.find(nextPath, pathParams);
                if (handler != null) {
                    pathParams.put(dynamicNode.param, nextPath.value);
                    return handler;
                }
            }
        }

        if (wildcardNode != null) {
            pathParams.put(wildcardNode.param, nextPath.subPath());
            return wildcardNode.handler;
        }

        return null;
    }

    static class DynamicNode extends PathNode {
        final String param;
        final Pattern pattern;

        DynamicNode(String param, String pattern) {
            this.param = param;
            if (pattern == null) this.pattern = null;
            else this.pattern = Pattern.compile(pattern);
        }

        boolean match(String path) {
            return pattern == null || pattern.matcher(path).matches();
        }
    }
}
