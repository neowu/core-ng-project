package core.framework.impl.web.response;

import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.impl.web.site.TemplateManager;

/**
 * @author neo
 */
final class ResponseHandlerContext {
    final ResponseBeanMapper responseBeanMapper;
    final TemplateManager templateManager;

    ResponseHandlerContext(ResponseBeanMapper responseBeanMapper, TemplateManager templateManager) {
        this.responseBeanMapper = responseBeanMapper;
        this.templateManager = templateManager;
    }
}
