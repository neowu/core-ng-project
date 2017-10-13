package core.framework.impl.web.service;

import core.framework.api.http.HTTPStatus;
import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.api.web.service.DELETE;
import core.framework.api.web.service.GET;
import core.framework.api.web.service.PUT;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.api.web.service.QueryParam;
import core.framework.api.web.service.ResponseStatus;

import java.util.List;

/**
 * @author neo
 */
public interface TestWebService {
    @GET
    @Path("/test")
    TestResponse search(TestSearchRequest request);

    @GET
    @Path("/test/:id")
    TestResponse get(@PathParam("id") Integer id);

    @PUT
    @Path("/test/:id")
    @ResponseStatus(HTTPStatus.CREATED)
    void create(@PathParam("id") Integer id, TestRequest request);

    @DELETE
    @Path("/test/:id")
    void delete(@PathParam("id") String id);

    @PUT
    @Path("/test")
    List<TestResponse> batch(List<TestRequest> requests);

    class TestRequest {
        @NotNull
        @Property(name = "string_field")
        public String stringField;
    }

    class TestSearchRequest {
        @NotNull
        @QueryParam(name = "int_field")
        public Integer intField;
    }

    class TestResponse {
        @Property(name = "int_field")
        public Integer intField;
    }
}
