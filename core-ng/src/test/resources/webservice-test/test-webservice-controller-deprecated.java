public class TestWebServiceImpl$deprecated implements core.framework.web.Controller {
    private final core.framework.internal.web.service.TestWebService delegate;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(core.framework.internal.web.service.TestWebService.class);

    public TestWebServiceImpl$deprecated(core.framework.internal.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        logger.warn(core.framework.log.Markers.errorCode("DEPRECATION"), "web service has been deprecated, please notify consumer to update");
        Integer $id = core.framework.internal.web.service.PathParamHelper.toInt(request.pathParam("id"));
        java.util.Optional response = delegate.deprecated($id);
        return core.framework.web.Response.bean(response);
    }

}
