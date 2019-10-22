package core.framework.internal.web.response;

import core.framework.internal.web.bean.ResponseBeanMapper;
import core.framework.internal.web.site.TemplateManager;

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
