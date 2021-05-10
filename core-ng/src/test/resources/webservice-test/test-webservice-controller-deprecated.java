public class Controller$TestWebServiceImpl$deprecated implements core.framework.web.Controller {
    private final core.framework.internal.web.service.TestWebService delegate;

    public Controller$TestWebServiceImpl$deprecated(core.framework.internal.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        core.framework.internal.web.service.WebServiceController.logDeprecation("core.framework.internal.web.service.TestWebService.deprecated(Integer)");
        Integer $id = core.framework.internal.web.service.PathParamHelper.toInt(request.pathParam("id"));
        java.util.Optional response = delegate.deprecated($id);
        return core.framework.web.Response.bean(response);
    }

}
