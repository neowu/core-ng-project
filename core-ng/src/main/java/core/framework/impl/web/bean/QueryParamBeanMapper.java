package core.framework.impl.web.bean;

import java.util.Map;

/**
 * @author neo
 */
interface QueryParamBeanMapper<T> {
    Map<String, String> toParams(T bean);

    T fromParams(Map<String, String> params);
}
