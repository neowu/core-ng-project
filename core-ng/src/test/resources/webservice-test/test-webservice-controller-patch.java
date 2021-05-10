public class Controller$TestWebServiceImpl$patch implements core.framework.web.Controller {
    private final core.framework.internal.web.service.TestWebService delegate;

    public Controller$TestWebServiceImpl$patch(core.framework.internal.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        Integer $id = core.framework.internal.web.service.PathParamHelper.toInt(request.pathParam("id"));
        core.framework.internal.web.service.TestWebService.TestRequest bean = (core.framework.internal.web.service.TestWebService.TestRequest) request.bean(core.framework.internal.web.service.TestWebService.TestRequest.class);
        delegate.patch($id, bean);
        return core.framework.web.Response.empty();
    }

}
