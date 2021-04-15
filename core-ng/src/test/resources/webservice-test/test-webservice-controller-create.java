public class Controller$TestWebServiceImpl$create implements core.framework.web.Controller {
    private final core.framework.internal.web.service.TestWebService delegate;

    public Controller$TestWebServiceImpl$create(core.framework.internal.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        Integer $id = core.framework.internal.web.service.PathParamHelper.toInt(request.pathParam("id"));
        core.framework.internal.web.service.TestWebService.TestRequest bean = (core.framework.internal.web.service.TestWebService.TestRequest) request.bean(core.framework.internal.web.service.TestWebService.TestRequest.class);
        delegate.create($id, bean);
        return core.framework.web.Response.empty().status(core.framework.api.http.HTTPStatus.CREATED);
    }

}
