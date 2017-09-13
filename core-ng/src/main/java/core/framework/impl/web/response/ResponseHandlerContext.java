package core.framework.impl.web.response;

import core.framework.impl.web.bean.ResponseBeanTypeValidator;
import core.framework.impl.web.site.TemplateManager;

/**
 * @author neo
 */
final class ResponseHandlerContext {
    final ResponseBeanTypeValidator validator;
    final TemplateManager templateManager;

    ResponseHandlerContext(ResponseBeanTypeValidator validator, TemplateManager templateManager) {
        this.validator = validator;
        this.templateManager = templateManager;
    }
}
