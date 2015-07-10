package core.framework.impl.template;

import java.util.Map;

/**
 * @author neo
 */
public interface TagHandler {
    void process(StringBuilder builder, Map<String, Object> context);
}
