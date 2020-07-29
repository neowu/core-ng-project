package core.framework.internal.web.bean;

import java.util.Map;

/**
 * @author neo
 */
public interface QueryParamWriter<T> {
    Map<String, String> toParams(T bean);
}
