public class Controller$TestWebServiceImpl$getEnum implements core.framework.web.Controller {
    private final core.framework.internal.web.service.TestWebService delegate;

    public Controller$TestWebServiceImpl$getEnum(core.framework.internal.web.service.TestWebService $1) {
        this.delegate = $1;
    }

    public core.framework.web.Response execute(core.framework.web.Request request) throws Exception {
        Long $id = core.framework.internal.web.service.PathParamHelper.toLong(request.pathParam("id"));
        core.framework.internal.web.service.TestWebService.TestEnum $enum = (core.framework.internal.web.service.TestWebService.TestEnum)core.framework.internal.web.service.PathParamHelper.toEnum(request.pathParam("enum"), core.framework.internal.web.service.TestWebService.TestEnum.class);
        core.framework.internal.web.service.TestWebService.TestResponse response = delegate.getEnum($id, $enum);
        return core.framework.web.Response.bean(response);
    }

}
